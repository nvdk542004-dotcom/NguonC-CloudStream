# Nguồn C – CloudStream GitHub Actions Project

Project này được chuẩn bị để build provider **Nguồn C** thành file `.cs3` bằng GitHub Actions.

## Nguồn dữ liệu

- Website: https://phim.nguonc.com/
- Phim mới: `https://phim.nguonc.com/api/films/phim-moi-cap-nhat?page={page}`
- Tìm kiếm: `https://phim.nguonc.com/api/films/search?keyword={keyword}`
- Chi tiết: `https://phim.nguonc.com/api/film/{slug}`

Provider đọc:

`movie.episodes[].items[].embed`

và đưa URL embed cho extractor của CloudStream.

## Cách dùng

1. Tạo một repository GitHub mới.
2. Upload toàn bộ file/thư mục trong project này.
3. Vào **Settings → Actions → General** và bật GitHub Actions.
4. Trong **Workflow permissions**, chọn **Read and write permissions**.
5. Push lên `main` hoặc `master`.
6. Workflow sẽ build plugin.
7. File `.cs3` và `plugins.json` được đưa vào branch `builds`.

CloudStream hỗ trợ repository JSON dạng:

```json
{
  "name": "Nguồn C",
  "description": "Nguồn C CloudStream repository",
  "manifestVersion": 1,
  "pluginLists": [
    "https://raw.githubusercontent.com/YOUR_USERNAME/YOUR_REPO/builds/plugins.json"
  ]
}
```

Sau khi GitHub Actions chạy thành công, bạn có thể dùng URL `repo.json`
ở branch `builds` làm repository URL trong CloudStream.

## Lưu ý

Provider này chỉ đọc dữ liệu do API Nguồn C trả về; nó không host video.

Hãy sử dụng nguồn nội dung theo quyền truy cập và điều khoản áp dụng.
