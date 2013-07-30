package kr.elara.android.framework.provider;

import java.util.List;

/**
 * Provide a list of Entities which are used for creating tables of database.
 */
public interface EntityHolder {

    List<Class<? extends Entity>> getEntities();

}
