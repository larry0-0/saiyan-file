package co.mgentertainment.file.web.cache;

import java.util.Optional;

/**
 * @author cl
 * @createTime 2022/12/7
 * @description UserHolder
 */
public class ClientHolder {

    private static ThreadLocal<String> CLIENT_HOLDER = new ThreadLocal<>();

    public static Optional<String> getCurrentClient() {
        return Optional.ofNullable(CLIENT_HOLDER.get());
    }

    public static void setCurrentClient(String appCode) {
        CLIENT_HOLDER.set(appCode);
    }

    public static void remove() {
        CLIENT_HOLDER.remove();
    }

}
