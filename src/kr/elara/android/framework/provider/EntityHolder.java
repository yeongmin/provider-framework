package kr.elara.android.framework.provider;

import java.util.List;

/**
 * Provide a list of Entity which is used for initializing ContentProvider.
 */
public interface EntityHolder {

    List<Class<? extends Entity>> getEntities();

}
