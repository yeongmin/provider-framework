package kr.elara.android.framework.provider.sample;

import kr.elara.android.framework.provider.Entity;
import kr.elara.android.framework.provider.EntityHolder;

import java.util.ArrayList;
import java.util.List;

public class EntityHolderImpl implements EntityHolder {

    @Override
    public List<Class<? extends Entity>> getEntities() {

        ArrayList<Class<? extends Entity>> classes = new ArrayList<Class<? extends Entity>>(2) {
            {
                add(Coffee.class);
                add(Juice.class);
            }
        };

        return classes;
    }
}
