# God Converter

God Converter is a command-line tool that converts documents into PDF format before being uploaded to MinIO using LibreOffice. It is designed for automation, backend workflows, and integration with data pipelines.

---

## Features

- Converts supported documents (like DOCX, PPTX, XLSX, XML, RTF) into PDFs
- Connects to MinIO (S3-compatible object storage)
- Accepts a folder prefix to filter input files
- Saves converted files locally
- Future enhancements include:
  - Resume support
  - Spark job integration
  - Support for batch processing

---

#### Sample files on MinIO (converted to pdf):
<img width="1348" height="614" alt="Screenshot_20250801_163259" src="https://github.com/user-attachments/assets/c6bc1736-6db2-4327-85d6-41c911754880" />

### Just clone it

```bash
git clone https://github.com/Zh1end22/god-converter.git
cd god-converter

