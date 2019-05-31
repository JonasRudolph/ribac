package codes.rudolph.ribac.server.user;

import codes.rudolph.ribac.jooq.tables.records.RibacUserRecord;
import codes.rudolph.ribac.server.DbHelper;
import codes.rudolph.ribac.server.Logger;
import codes.rudolph.ribac.server.error.DuplicateCreateError;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.reactivex.Single;
import org.jooq.exception.DataAccessException;

import java.sql.SQLException;

import static codes.rudolph.ribac.jooq.tables.RibacUser.RIBAC_USER;

public class UserRepository {

    private final Logger log;

    private final DbHelper dbHelper;



    @Inject
    public UserRepository(
        @Named("dbLogger") Logger log,
        DbHelper dbHelper
    ) {
        this.log = log;
        this.dbHelper = dbHelper;
    }



    public Single<RibacUserRecord> createUser(String externalId, String requestId) {
        final var externalRequestId = log.createExternalRequestId(requestId);

        log.start("Creating User", externalRequestId);
        return this.dbHelper
                   .execute(
                       db -> db.insertInto(RIBAC_USER)
                               .set(RIBAC_USER.EXTERNAL_ID, externalId)
                               .returning()
                               .fetchOne()
                   )
                   .doOnSuccess(log.endSuccessfully("Created User", externalRequestId))
                   .doOnError(log.endFailed("To create User", externalRequestId))
                   .onErrorResumeNext(
                       failure -> Single.error(
                           UserRepository.mySqlRespondedWithDuplicateEntryError(failure)
                               ? new DuplicateCreateError("A user already exists with the id '" + externalId + "'")
                               : failure
                       )
                   );
    }



    public Single<RibacUserRecord> getUser(String externalId, String requestId) {
        final var externalRequestId = log.createExternalRequestId(requestId);

        log.start("Getting User", externalRequestId);
        return this.dbHelper
                   .execute(
                       db -> db.selectFrom(RIBAC_USER)
                               .where(RIBAC_USER.EXTERNAL_ID.eq(externalId))
                               .fetchOne()
                   )
                   .doOnSuccess(log.endSuccessfully("Got User", externalRequestId))
                   .doOnError(log.endFailed("To get User", externalRequestId));
    }



    private static boolean mySqlRespondedWithDuplicateEntryError(Throwable failure) {
        /*
         * @see https://dev.mysql.com/doc/refman/8.0/en/server-error-reference.html#error_er_dup_entry
         */
        final int MYSQL_DUPLICATE_ENTRY_CODE = 1062;

        return (failure instanceof DataAccessException)
                   && (failure.getCause() instanceof SQLException)
                   && (((SQLException) failure.getCause()).getErrorCode() == MYSQL_DUPLICATE_ENTRY_CODE);
    }
}
