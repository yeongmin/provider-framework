package kr.elara.android.framework.provider;

public class Property {

    private final String mAuthority;
    private final String mDatabaseName;
    private final String mDatabaseVersion;

    public Property(Builder builder) {
        mAuthority = builder.mAuthority;
        mDatabaseName = builder.mDatabaseName;
        mDatabaseVersion = builder.mDatabaseVersion;
    }

    public String getAuthority() {
        return mAuthority;
    }

    public String getDatabaseName() {
        return mDatabaseName;
    }

    public String getDatabaseVersion() {
        return mDatabaseVersion;
    }

    public static class Builder {

        private final String mAuthority;
        private String mDatabaseName = "default.db";
        private String mDatabaseVersion = "1";


        public Builder(String authority) {
            mAuthority = authority;
        }

        public void setDatabaseName(String name) {
            mDatabaseName = name;
        }

        public void setDatabaseVersion(String version) {
            mDatabaseVersion = version;
        }

        public Property build() {
            return new Property(this);
        }
    }
}
