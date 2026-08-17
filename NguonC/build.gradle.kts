version = 1

cloudstream {
    description = "Provider Nguồn C – tìm kiếm, phim mới và phát các tập từ API Nguồn C."
    authors = listOf("NguonC Community")
    status = 1
    tvTypes = listOf("Movie", "TvSeries")
    requiresResources = false
    language = "vi"
    iconUrl = "https://phim.nguonc.com/public/images/Film/hoa-thien-cot-co-trang-2015-thumb.jpg"
}

android {
    buildFeatures {
        buildConfig = true
    }
}
