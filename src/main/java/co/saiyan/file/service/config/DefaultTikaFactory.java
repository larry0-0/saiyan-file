package co.saiyan.file.service.config;

import co.saiyan.common.fileupload.tika.TikaFactory;
import org.apache.tika.Tika;

/**
 * @author larry
 * @createTime 2023/9/15
 * @description DefaultTikaFactory
 */
public class DefaultTikaFactory implements TikaFactory {
    private volatile Tika tika;

    @Override
    public Tika getTika() {
        if (tika == null) {
            synchronized (this) {
                if (tika == null) {
                    tika = new Tika();
                }
            }
        }
        return tika;
    }
}
