package kr.elara.android.framework.provider.test;

import kr.elara.android.framework.provider.AbstractContentProvider;
import kr.elara.android.framework.provider.Entity;
import kr.elara.android.framework.provider.EntityHolder;
import kr.elara.android.framework.provider.Property;

import java.util.ArrayList;
import java.util.List;

public class MockContentProvider extends AbstractContentProvider {

    public static final String AUTHORITY = "kr.elara.test";

    @Override
    protected Property getProperty() {
        return new Property.Builder(AUTHORITY).build();
    }

    @Override
    protected EntityHolder getEntityHolder() {
        return new EntityHolder() {
            @Override
            public List<Class<? extends Entity>> getEntities() {
                return new ArrayList<Class<? extends Entity>>() {
                    {
                        add(Item.class);
                    }
                };
            }
        };
    }

}
