package com.example.tesseractoncf;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PreDestroy;
import java.io.File;

@Component
@RequestScope
public class Ocr {
    private final Logger log = LoggerFactory.getLogger(Ocr.class);
    private final tesseract.TessBaseAPI api;

    public Ocr(@Value("${tessdata.dir:classpath:/tessdata}") File tessdata, @Value("${tessdata.lang:eng}") String lang) {
        this.api = new tesseract.TessBaseAPI();
        String path = tessdata.toPath().toString();
        log.debug("Load tessdata from {} (lang={}).", path, lang);
        if (this.api.Init(path, lang) != 0) {
            throw new IllegalStateException("Could not initialize tesseract.");
        }
    }

    public String read(File file) {
        BytePointer outText = null;
        lept.PIX image = null;
        try {
            image = lept.pixRead(file.getAbsolutePath());
            this.api.SetImage(image);
            // Get OCR result
            outText = this.api.GetUTF8Text();
            return outText == null ? "" : outText.getString();
        } finally {
            if (outText != null) {
                outText.deallocate();
            }
            if (image != null) {
                lept.pixDestroy(image);
            }
        }
    }

    @PreDestroy
    void cleanup() {
        log.debug("Cleanup TessBaseAPI.");
        api.End();
    }

}
