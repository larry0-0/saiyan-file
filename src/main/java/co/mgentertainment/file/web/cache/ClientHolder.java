package co.mgentertainment.file.web.cache;

import co.mgentertainment.file.service.event.listener.DistributedEventKey;

import java.util.Optional;

/**
 * @author larry
 * @createTime 2022/12/7
 * @description UserHolder
 */
public class ClientHolder {

    private static ThreadLocal<String> CLIENT_HOLDER = new ThreadLocal<>();

    public static String getCurrentClient() {
        return Optional.ofNullable(CLIENT_HOLDER.get()).orElse(DistributedEventKey.UPLOADS);
    }

    public static void setCurrentClient(String appCode) {
        CLIENT_HOLDER.set(appCode);
    }

    public static void remove() {
        CLIENT_HOLDER.remove();
    }

}
