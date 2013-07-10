package kr.elara.android.framework.provider;

/**
 * Properties which are used for representing database.
 */
public class Property {

    private final String mAuthority;
    private final String mDatabaseName;
    private final int mDatabaseVersion;
    private final DatabaseUpdateStrategy mUpdateStrategy;

    private Property(Builder builder) {
        mAuthority = builder.mAuthority;
        mDatabaseName = builder.mDatabaseName;
        mDatabaseVersion = builder.mDatabaseVersion;
        mUpdateStrategy = builder.mUpdateStrategy;
    }

    public String getAuthority() {
        return mAuthority;
    }

    public String getDatabaseName() {
        return mDatabaseName;
    }

    public int getDatabaseVersion() {
        return mDatabaseVersion;
    }

    public DatabaseUpdateStrategy getDatabaseUpdateStrategy() {
        return mUpdateStrategy;
    }

    public static class Builder {

        private final String mAuthority;
        private String mDatabaseName = "default.db";
        private int mDatabaseVersion = 1;
        private DatabaseUpdateStrategy mUpdateStrategy;


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

        public Builder setDatabaseVersion(int version) {
            mDatabaseVersion = version;
            return this;
        }

        public Builder setUpdateStrategy(DatabaseUpdateStrategy updateStrategy) {
            mUpdateStrategy = updateStrategy;
            return this;
        }

        public Property build() {
            return new Property(this);
        }
    }
}
