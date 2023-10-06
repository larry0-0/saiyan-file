package co.mgentertainment.file.service.config;

import co.mgentertainment.common.fileupload.tika.ContentTypeDetect;
import co.mgentertainment.common.fileupload.tika.TikaFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author larry
 * @createTime 2023/9/15
 * @description TikaContentTypeDetect
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TikaContentTypeDetect implements ContentTypeDetect {
    private TikaFactory tikaFactory;

    @Override
    public String detect(File file) throws IOException {
        return tikaFactory.getTika().detect(file);
    }

    @Override
    public String detect(byte[] bytes) {
        return tikaFactory.getTika().detect(bytes);
    }

    @Override
    public String detect(byte[] bytes, String filename) {
        return tikaFactory.getTika().detect(bytes, filename);
    }

    @Override
    public String detect(InputStream in, String filename) throws IOException {
        return tikaFactory.getTika().detect(in, filename);
    }
}