package co.mgentertainment.file.web.cache;

/**
 * @author larry
 * @createTime 2022/12/7
 * @description UserHolder
 */
public class ClientHolder {

    private static ThreadLocal<String> CLIENT_HOLDER = new ThreadLocal<>();

    public static String getCurrentClient() {
        return CLIENT_HOLDER.get();
    }

    public static void setCurrentClient(String appCode) {
        CLIENT_HOLDER.set(appCode);
    }

    public static void remove() {
        CLIENT_HOLDER.remove();
    }

}
