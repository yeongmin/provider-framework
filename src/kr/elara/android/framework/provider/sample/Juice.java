package kr.elara.android.framework.provider.sample;

import com.j256.ormlite.table.DatabaseTable;
import kr.elara.android.framework.provider.Entity;
import kr.elara.android.framework.provider.annotation.MimeType;
import kr.elara.android.framework.provider.annotation.UriPath;

@DatabaseTable(tableName = Juice.JUICE)
@UriPath({Juice.JUICE, Juice.JUICE + "/#"})
@MimeType(Juice.JUICE)
public class Juice implements Entity {

    public static final String JUICE = "juices";
}
