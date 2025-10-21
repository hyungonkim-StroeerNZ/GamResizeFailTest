package sport1.mobile.android.app

import org.json.JSONArray
import org.json.JSONObject
import org.prebid.mobile.TargetingParams

class OrtbJsonHelper private constructor() {

    companion object {
        private val lock = Any()
        private var root: JSONObject = JSONObject()

        @JvmStatic
        fun clear() {
            synchronized(lock) {
                root = JSONObject()
                apply()
            }
        }

        @JvmStatic
        fun apply() {
            synchronized(lock) {
                TargetingParams.setGlobalOrtbConfig(root.toString())
            }
        }

        @JvmStatic
        fun addOrUpdate(value: String, vararg path: String) {
            addOrUpdateInternal(value, *path)
        }

        @JvmStatic
        fun addOrUpdate(value: Number, vararg path: String) {
            addOrUpdateInternal(value, *path)
        }

        @JvmStatic
        fun addOrUpdate(value: Boolean, vararg path: String) {
            addOrUpdateInternal(value, *path)
        }

        @JvmStatic
        fun addOrUpdate(value: JSONObject, vararg path: String) {
            addOrUpdateInternal(value, *path)
        }

        @JvmStatic
        fun addOrUpdate(value: JSONArray, vararg path: String) {
            addOrUpdateInternal(value, *path)
        }

        @JvmStatic
        fun findString(vararg path: String): String? {
            val v = findObject(*path)
            return v as? String
        }

        @JvmStatic
        fun findNumber(vararg path: String): Number? {
            val v = findObject(*path)
            return v as? Number
        }

        @JvmStatic
        fun findBoolean(vararg path: String): Boolean? {
            val v = findObject(*path)
            return v as? Boolean
        }

        @JvmStatic
        fun findJsonObject(vararg path: String): JSONObject? {
            val v = findObject(*path)
            return v as? JSONObject
        }

        @JvmStatic
        fun findJsonArray(vararg path: String): JSONArray? {
            val v = findObject(*path)
            return v as? JSONArray
        }

        @JvmStatic
        fun remove(vararg path: String): Boolean {
            synchronized(lock) {
                if (path.isEmpty()) return false
                val parent = descendCreating(false, path, path.size - 1) ?: return false
                val old = parent.remove(path.last())
                apply()
                return old != null
            }
        }

        @JvmStatic
        fun snapshot(): JSONObject {
            synchronized(lock) {
                return JSONObject(root.toString())
            }
        }

        private fun addOrUpdateInternal(value: Any, vararg path: String) {
            synchronized(lock) {
                if (path.isEmpty()) return
                val parent = descendCreating(true, path, path.size - 1) ?: return
                parent.put(path.last(), value)
                apply()
            }
        }

        private fun findObject(vararg path: String): Any? {
            synchronized(lock) {
                if (path.isEmpty()) return null
                var cur = root
                for (i in 0 until path.size - 1) {
                    val next = cur.opt(path[i])
                    if (next !is JSONObject) return null
                    cur = next
                }
                return cur.opt(path.last())
            }
        }

        private fun descendCreating(create: Boolean, path: Array<out String>, endExclusive: Int): JSONObject? {
            var cur = root
            for (i in 0 until endExclusive) {
                val next = cur.opt(path[i])
                if (next !is JSONObject) {
                    if (!create) return null
                    val fresh = JSONObject()
                    cur.put(path[i], fresh)
                    cur = fresh
                } else {
                    cur = next
                }
            }
            return cur
        }
    }
}