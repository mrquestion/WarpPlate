package mrq.plugin.minecraft.move.i18n;

public enum Messages {
    DEFAULT_LOCALE("ko_kr"),
    LOCALE_LOCAL("locale.local"), LOCALE_INTERNATIONAL("locale.international"),
    INFORMATION_VERSION("information.version"),
    INFORMATION_COUNT("information.count"),
    ADD_NEW_PLATE("add.new.plate"), ADD_DUPLICATED("add.duplicated"),
    SOME_PLATE_DESTROYED("some.plate.destroyed"), YOUR_PLATE_DESTROYED("your.plate.destroyed"),
    PLATE_ENABLED("plate.enabled"), PLATE_DISABLED("plate.disabled"),
    PLATE_NOT_FOUND("plate.not.found"),
    TELEPORT_COORDINATES("teleport.coordinates"),
    TELEPORT_ALIAS("teleport.alias"),
    TELEPORT_HOME("teleport.home"),
    ;

    private String message;

    Messages(String message) {
        setMessage(message);
    }

    private String getMessage() {
        return message;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
