package kr.elara.android.framework.provider;

/**
 * Properties which are used for representing database.
 */
public class Property {

    private final String mAuthority;
    private final String mDatabaseName;
    private final String mDatabaseVersion;

    private Property(Builder builder) {
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
            if (authority == null || (authority != null && authority.isEmpty())) {
                throw new IllegalArgumentException("Authority cannot be null or empty.");
            }
            mAuthority = authority;
        }

        public Builder setDatabaseName(String name) {
            mDatabaseName = name;
            return this;
        }

        public Builder setDatabaseVersion(String version) {
            mDatabaseVersion = version;
            return this;
        }

        public Property build() {
            return new Property(this);
        }
    }
}
