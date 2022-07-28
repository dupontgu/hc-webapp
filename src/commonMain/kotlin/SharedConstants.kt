const val UPLOAD_ENDPOINT = "/upload"
const val HEADER_CONTENT_DISPOSITION = "Content-Disposition"
const val HEADER_ATTACHMENT = "attachment"
const val HTCLP_SUFFIX = "htclp"

const val START_PAGE_PARAM = "start"
enum class StartPage(val key: String) {
    DEFAULT("default"), UPLOAD("upload"), SETUP("setup");

    companion object {
        fun from(type: String?): StartPage = values().firstOrNull { it.key == type } ?: DEFAULT
    }
}