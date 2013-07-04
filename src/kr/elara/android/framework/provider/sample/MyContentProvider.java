package kr.elara.android.framework.provider.sample;

import kr.elara.android.framework.provider.AbstractContentProvider;
import kr.elara.android.framework.provider.EntityHolder;
import kr.elara.android.framework.provider.Property;

public class MyContentProvider extends AbstractContentProvider {

    public static final String AUTHORITY = "kr.elara.android.sample";

    @Override
    protected Property getProperty() {
        return new Property.Builder(AUTHORITY).setDatabaseName("main.db").build();
    }

    @Override
    protected EntityHolder getEntityHolder() {
        return new EntityHolderImpl();
    }
}
