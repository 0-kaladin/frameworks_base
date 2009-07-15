/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package android.provider;

import android.content.Intent;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.accounts.Account;
import android.os.RemoteException;

/**
 * The contract between the contacts provider and applications. Contains definitions
 * for the supported URIs and columns.
 *
 * @hide
 */
public final class ContactsContract {
    /** The authority for the contacts provider */
    public static final String AUTHORITY = "com.android.contacts";
    /** A content:// style uri to the authority for the contacts provider */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public interface SyncStateColumns extends SyncStateContract.Columns {
    }

    public static final class SyncState {
        /**
         * This utility class cannot be instantiated
         */
        private SyncState() {}

        public static final String CONTENT_DIRECTORY =
                SyncStateContract.Constants.CONTENT_DIRECTORY;

        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(AUTHORITY_URI, CONTENT_DIRECTORY);

        /**
         * @see android.provider.SyncStateContract.Helpers#get
         */
        public static byte[] get(ContentProviderClient provider, Account account)
                throws RemoteException {
            return SyncStateContract.Helpers.get(provider, CONTENT_URI, account);
        }

        /**
         * @see android.provider.SyncStateContract.Helpers#set
         */
        public static void set(ContentProviderClient provider, Account account, byte[] data)
                throws RemoteException {
            SyncStateContract.Helpers.set(provider, CONTENT_URI, account, data);
        }

        /**
         * @see android.provider.SyncStateContract.Helpers#newSetOperation
         */
        public static ContentProviderOperation newSetOperation(Account account, byte[] data) {
            return SyncStateContract.Helpers.newSetOperation(CONTENT_URI, account, data);
        }
    }

    public interface AggregatesColumns {
        /**
         * The display name for the contact.
         * <P>Type: TEXT</P>
         */
        public static final String DISPLAY_NAME = "display_name";

        /**
         * The number of times a person has been contacted
         * <P>Type: INTEGER</P>
         */
        public static final String TIMES_CONTACTED = "times_contacted";

        /**
         * The last time a person was contacted.
         * <P>Type: INTEGER</P>
         */
        public static final String LAST_TIME_CONTACTED = "last_time_contacted";

        /**
         * Is the contact starred?
         * <P>Type: INTEGER (boolean)</P>
         */
        public static final String STARRED = "starred";

        /**
         * A custom ringtone associated with a person. Not always present.
         * <P>Type: TEXT (URI to the ringtone)</P>
         */
        public static final String CUSTOM_RINGTONE = "custom_ringtone";

        /**
         * Whether the person should always be sent to voicemail. Not always
         * present.
         * <P>Type: INTEGER (0 for false, 1 for true)</P>
         */
        public static final String SEND_TO_VOICEMAIL = "send_to_voicemail";

        /**
         * Reference to the row in the data table holding the primary phone number.
         * <P>Type: INTEGER REFERENCES data(_id)</P>
         */
        public static final String PRIMARY_PHONE_ID = "primary_phone_id";

        /**
         * Reference to the row in the data table holding the primary email address.
         * <P>Type: INTEGER REFERENCES data(_id)</P>
         */
        public static final String PRIMARY_EMAIL_ID = "primary_email_id";

        /**
         * Reference to the row in the data table holding the photo.
         * <P>Type: INTEGER REFERENCES data(_id)</P>
         */
        public static final String PHOTO_ID = "photo_id";

        /**
         * Lookup value that reflects the {@link Groups#MEMBERS_VISIBLE} state
         * of any {@link GroupMembership} for this aggregate.
         */
        public static final String IN_VISIBLE_GROUP = "in_visible_group";
    }

    /**
     * Constants for the aggregates table, which contains a record per group
     * of contact representing the same person.
     */
    public static final class Aggregates implements BaseColumns, AggregatesColumns {
        /**
         * This utility class cannot be instantiated
         */
        private Aggregates()  {}

        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "aggregates");

        /**
         * The content:// style URI for this table joined with useful data from
         * {@link Data} and {@link Presence}.
         */
        public static final Uri CONTENT_SUMMARY_URI = Uri.withAppendedPath(AUTHORITY_URI,
                "aggregates_summary");
        /**
         * The content:// style URI used for "type-to-filter" functionality on the
         * {@link CONTENT_SUMMARY_URI} URI. The filter string will be used to match
         * various parts of the aggregate name. The filter argument should be passed
         * as an additional path segment after this URI.
         */
        public static final Uri CONTENT_SUMMARY_FILTER_URI = Uri.withAppendedPath(
                CONTENT_SUMMARY_URI, "filter");
        /**
         * The content:// style URI for this table joined with useful data from
         * {@link Data} and {@link Presence}, filtered to include only starred aggregates
         * and the most frequently contacted aggregates.
         */
        public static final Uri CONTENT_SUMMARY_STREQUENT_URI = Uri.withAppendedPath(
                CONTENT_SUMMARY_URI, "strequent");
        /**
         * The content:// style URI used for "type-to-filter" functionality on the
         * {@link CONTENT_SUMMARY_STREQUENT_URI} URI. The filter string will be used to match
         * various parts of the aggregate name. The filter argument should be passed
         * as an additional path segment after this URI.
         */
        public static final Uri CONTENT_SUMMARY_STREQUENT_FILTER_URI = Uri.withAppendedPath(
                CONTENT_SUMMARY_STREQUENT_URI, "filter");

        public static final Uri CONTENT_SUMMARY_GROUP_URI = Uri.withAppendedPath(
                CONTENT_SUMMARY_URI, "group");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * people.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/person_aggregate";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * person.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/person_aggregate";

        /**
         * A sub-directory of a single contact aggregate that contains all of their
         * {@link Data} rows.
         */
        public static final class Data implements BaseColumns, DataColumns {
            /**
             * no public constructor since this is a utility class
             */
            private Data() {}

            /**
             * The directory twig for this sub-table
             */
            public static final String CONTENT_DIRECTORY = "data";
        }

        /**
         * A sub-directory of a single contact aggregate that contains all aggregation suggestions
         * (other aggregates).  The aggregation suggestions are computed based on approximate
         * data matches with this aggregate.
         */
        public static final class AggregationSuggestions implements BaseColumns, AggregatesColumns {
            /**
             * No public constructor since this is a utility class
             */
            private AggregationSuggestions() {}

            /**
             * The directory twig for this sub-table
             */
            public static final String CONTENT_DIRECTORY = "suggestions";

            /**
             * An optional query parameter that can be supplied to limit the number of returned
             * suggestions.
             * <p>
             * Type: INTEGER
             */
            public static final String MAX_SUGGESTIONS = "max_suggestions";
        }
    }

    /**
     * Constants for the contacts table, which contains the base contact information.
     */
    public static final class Contacts implements BaseColumns {
        /**
         * This utility class cannot be instantiated
         */
        private Contacts()  {}

        /**
         * The package name that owns this contact and all of its details. This
         * package has control over the {@link #IS_RESTRICTED} flag, and can
         * grant {@link RestrictionExceptions} to other packages.
         */
        public static final String PACKAGE = "package";

        /**
         * Flag indicating that this data entry has been restricted by the owner
         * {@link #PACKAGE}.
         */
        public static final String IS_RESTRICTED = "is_restricted";

        /**
         * A reference to the name of the account to which this data belongs
         */
        public static final String ACCOUNT_NAME = "account_name";

        /**
         * A reference to the type of the account to which this data belongs
         */
        public static final String ACCOUNT_TYPE = "account_type";

        /**
         * A reference to the {@link Aggregates#_ID} that this data belongs to.
         */
        public static final String AGGREGATE_ID = "aggregate_id";

        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "contacts");

        /**
         * The content:// style URL for filtering people by email address. The
         * filter argument should be passed as an additional path segment after
         * this URI.
         *
         * @hide
         */
        public static final Uri CONTENT_FILTER_EMAIL_URI =
                Uri.withAppendedPath(CONTENT_URI, "filter_email");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * people.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/person";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * person.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/person";

        /**
         * A string that uniquely identifies this contact to its source, which is referred to
         * by the {@link #ACCOUNT_NAME} and {@link #ACCOUNT_TYPE}
         */
        public static final String SOURCE_ID = "sourceid";

        /**
         * An integer that is updated whenever this contact or its data changes.
         */
        public static final String VERSION = "version";

        /**
         * Set to 1 whenever the version changes
         */
        public static final String DIRTY = "dirty";

        /**
         * A sub-directory of a single contact that contains all of their {@link Data} rows.
         * To access this directory append
         */
        public static final class Data implements BaseColumns, DataColumns {
            /**
             * no public constructor since this is a utility class
             */
            private Data() {}

            /**
             * The directory twig for this sub-table
             */
            public static final String CONTENT_DIRECTORY = "data";
        }
    }

    private interface DataColumns {
        /**
         * The mime-type of the item represented by this row.
         */
        public static final String MIMETYPE = "mimetype";

        /**
         * A reference to the {@link android.provider.ContactsContract.Contacts#_ID}
         * that this data belongs to.
         */
        public static final String CONTACT_ID = "contact_id";

        /**
         * Whether this is the primary entry of its kind for the contact it belongs to
         * <P>Type: INTEGER (if set, non-0 means true)</P>
         */
        public static final String IS_PRIMARY = "is_primary";

        /**
         * Whether this is the primary entry of its kind for the aggregate it belongs to. Any data
         * record that is "super primary" must also be "primary".
         * <P>Type: INTEGER (if set, non-0 means true)</P>
         */
        public static final String IS_SUPER_PRIMARY = "is_super_primary";

        /**
         * The version of this data record. This is a read-only value. The data column is
         * guaranteed to not change without the version going up. This value is monotonically
         * increasing.
         * <P>Type: INTEGER</P>
         */
        public static final String DATA_VERSION = "data_version";

        /** Generic data column, the meaning is {@link #MIMETYPE} specific */
        public static final String DATA1 = "data1";
        /** Generic data column, the meaning is {@link #MIMETYPE} specific */
        public static final String DATA2 = "data2";
        /** Generic data column, the meaning is {@link #MIMETYPE} specific */
        public static final String DATA3 = "data3";
        /** Generic data column, the meaning is {@link #MIMETYPE} specific */
        public static final String DATA4 = "data4";
        /** Generic data column, the meaning is {@link #MIMETYPE} specific */
        public static final String DATA5 = "data5";
        /** Generic data column, the meaning is {@link #MIMETYPE} specific */
        public static final String DATA6 = "data6";
        /** Generic data column, the meaning is {@link #MIMETYPE} specific */
        public static final String DATA7 = "data7";
        /** Generic data column, the meaning is {@link #MIMETYPE} specific */
        public static final String DATA8 = "data8";
        /** Generic data column, the meaning is {@link #MIMETYPE} specific */
        public static final String DATA9 = "data9";
        /** Generic data column, the meaning is {@link #MIMETYPE} specific */
        public static final String DATA10 = "data10";
    }

    /**
     * Constants for the data table, which contains data points tied to a contact.
     * For example, a phone number or email address. Each row in this table contains a type
     * definition and some generic columns. Each data type can define the meaning for each of
     * the generic columns.
     */
    public static final class Data implements BaseColumns, DataColumns {
        /**
         * This utility class cannot be instantiated
         */
        private Data() {}

        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "data");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of data.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/data";
    }

    /**
     * A table that represents the result of looking up a phone number, for example for caller ID.
     * The table joins that data row for the phone number with the contact that owns the number.
     * To perform a lookup you must append the number you want to find to {@link #CONTENT_URI}.
     */
    public static final class PhoneLookup implements BaseColumns, DataColumns, AggregatesColumns {
        /**
         * This utility class cannot be instantiated
         */
        private PhoneLookup() {}

        /**
         * The content:// style URI for this table. Append the phone number you want to lookup
         * to this URI and query it to perform a lookup. For example:
         *
         * {@code
         * Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_URI, phoneNumber);
         * }
         */
        public static final Uri CONTENT_FILTER_URI = Uri.withAppendedPath(AUTHORITY_URI,
                "phone_lookup");
    }

    /**
     * Additional data mixed in with {@link Im.CommonPresenceColumns} to link
     * back to specific {@link ContactsContract.Aggregates#_ID} entries.
     */
    private interface PresenceColumns {
        /**
         * Reference to the {@link Aggregates#_ID} this presence references.
         */
        public static final String AGGREGATE_ID = "aggregate_id";

        /**
         * Reference to the {@link Data#_ID} entry that owns this presence.
         */
        public static final String DATA_ID = "data_id";

        /**
         * The IM service the presence is coming from. Formatted using either
         * {@link Contacts.ContactMethods#encodePredefinedImProtocol} or
         * {@link Contacts.ContactMethods#encodeCustomImProtocol}.
         * <p>
         * Type: STRING
         */
        public static final String IM_PROTOCOL = "im_protocol";

        /**
         * The IM handle the presence item is for. The handle is scoped to the
         * {@link #IM_PROTOCOL}.
         * <p>
         * Type: STRING
         */
        public static final String IM_HANDLE = "im_handle";

        /**
         * The IM account for the local user that the presence data came from.
         * <p>
         * Type: STRING
         */
        public static final String IM_ACCOUNT = "im_account";
    }

    public static final class Presence implements BaseColumns, PresenceColumns,
            Im.CommonPresenceColumns {
        /**
         * This utility class cannot be instantiated
         */
        private Presence() {
        }

        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "presence");

        /**
         * Gets the resource ID for the proper presence icon.
         *
         * @param status the status to get the icon for
         * @return the resource ID for the proper presence icon
         */
        public static final int getPresenceIconResourceId(int status) {
            switch (status) {
                case AVAILABLE:
                    return android.R.drawable.presence_online;
                case IDLE:
                case AWAY:
                    return android.R.drawable.presence_away;
                case DO_NOT_DISTURB:
                    return android.R.drawable.presence_busy;
                case INVISIBLE:
                    return android.R.drawable.presence_invisible;
                case OFFLINE:
                default:
                    return android.R.drawable.presence_offline;
            }
        }

        /**
         * Returns the precedence of the status code the higher number being the higher precedence.
         *
         * @param status The status code.
         * @return An integer representing the precedence, 0 being the lowest.
         */
        public static final int getPresencePrecedence(int status) {
            // Keep this function here incase we want to enforce a different precedence than the
            // natural order of the status constants.
            return status;
        }

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * presence details.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/im-presence";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * presence detail.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/im-presence";
    }

    /**
     * Container for definitions of common data types stored in the {@link Data} table.
     */
    public static final class CommonDataKinds {
        /**
         * The {@link Data#PACKAGE} value for common data that should be shown
         * using a default style.
         */
        public static final String PACKAGE_COMMON = "common";

        /**
         * Columns common across the specific types.
         */
        private interface BaseCommonColumns {
            /**
             * The package name that defines this type of data.
             */
            public static final String PACKAGE = "package";

            /**
             * The mime-type of the item represented by this row.
             */
            public static final String MIMETYPE = "mimetype";

            /**
             * A reference to the {@link android.provider.ContactsContract.Contacts#_ID} that this
             * data belongs to.
             */
            public static final String CONTACT_ID = "contact_id";
        }

        /**
         * Columns common across the specific types.
         */
        private interface CommonColumns {
            /**
             * The type of data, for example Home or Work.
             * <P>Type: INTEGER</P>
             */
            public static final String TYPE = "data1";

            /**
             * The data for the contact method.
             * <P>Type: TEXT</P>
             */
            public static final String DATA = "data2";

            /**
             * The user defined label for the the contact method.
             * <P>Type: TEXT</P>
             */
            public static final String LABEL = "data3";
        }

        /**
         * The base types that all "Typed" data kinds support.
         */
        public interface BaseTypes {

            /**
             * A custom type. The custom label should be supplied by user.
             */
            public static int TYPE_CUSTOM = 0;
        }

        /**
         * Parts of the name.
         */
        public static final class StructuredName {
            private StructuredName() {}

            /** Mime-type used when storing this in data table. */
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/name";

            /**
             * The given name for the contact.
             * <P>Type: TEXT</P>
             */
            public static final String GIVEN_NAME = "data1";

            /**
             * The family name for the contact.
             * <P>Type: TEXT</P>
             */
            public static final String FAMILY_NAME = "data2";

            /**
             * The contact's honorific prefix, e.g. "Sir"
             * <P>Type: TEXT</P>
             */
            public static final String PREFIX = "data3";

            /**
             * The contact's middle name
             * <P>Type: TEXT</P>
             */
            public static final String MIDDLE_NAME = "data4";

            /**
             * The contact's honorific suffix, e.g. "Jr"
             */
            public static final String SUFFIX = "data5";

            /**
             * The phonetic version of the given name for the contact.
             * <P>Type: TEXT</P>
             */
            public static final String PHONETIC_GIVEN_NAME = "data6";

            /**
             * The phonetic version of the additional name for the contact.
             * <P>Type: TEXT</P>
             */
            public static final String PHONETIC_MIDDLE_NAME = "data7";

            /**
             * The phonetic version of the family name for the contact.
             * <P>Type: TEXT</P>
             */
            public static final String PHONETIC_FAMILY_NAME = "data8";

            /**
             * The name that should be used to display the contact.
             * <P>Type: TEXT</P>
             */
            public static final String DISPLAY_NAME = "data9";
        }

        /**
         * A nickname.
         */
        public static final class Nickname implements BaseTypes {
            private Nickname() {}

            /** Mime-type used when storing this in data table. */
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/nickname";

            /**
             * The type of data, for example Home or Work.
             * <P>Type: INTEGER</P>
             */
            public static final String TYPE = "data1";

            public static final int TYPE_DEFAULT = 1;
            public static final int TYPE_OTHER_NAME = 2;
            public static final int TYPE_MAINDEN_NAME = 3;
            public static final int TYPE_SHORT_NAME = 4;
            public static final int TYPE_INITIALS = 5;

            /**
             * The name itself
             */
            public static final String NAME = "data2";

            /**
             * The user provided label, only used if TYPE is {@link #TYPE_CUSTOM}.
             * <P>Type: TEXT</P>
             */
            public static final String LABEL = "data3";
        }

        /**
         * Common data definition for telephone numbers.
         */
        public static final class Phone implements BaseCommonColumns, CommonColumns, BaseTypes {
            private Phone() {}

            /** Mime-type used when storing this in data table. */
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/phone";

            /**
             * The MIME type of {@link #CONTENT_URI} providing a directory of
             * phones.
             */
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/phone";

            /**
             * The content:// style URI for all data records of the
             * {@link Phone.CONTENT_ITEM_TYPE} mimetype, combined with the associated contact
             * and aggregate data.
             */
            public static final Uri CONTENT_URI = Uri.withAppendedPath(Data.CONTENT_URI,
                    "phones");

            /**
             * The content:// style URI for filtering data records of the
             * {@link Phone.CONTENT_ITEM_TYPE} mimetype, combined with the associated contact
             * and aggregate data. The filter argument should be passed
             * as an additional path segment after this URI.
             */
            public static final Uri CONTENT_FILTER_URI = Uri.withAppendedPath(CONTENT_URI,
                    "filter");

            public static final int TYPE_HOME = 1;
            public static final int TYPE_MOBILE = 2;
            public static final int TYPE_WORK = 3;
            public static final int TYPE_FAX_WORK = 4;
            public static final int TYPE_FAX_HOME = 5;
            public static final int TYPE_PAGER = 6;
            public static final int TYPE_OTHER = 7;

            /**
             * The phone number as the user entered it.
             * <P>Type: TEXT</P>
             */
            public static final String NUMBER = "data2";
        }

        /**
         * Common data definition for email addresses.
         */
        public static final class Email implements BaseCommonColumns, CommonColumns, BaseTypes {
            private Email() {}

            /** Mime-type used when storing this in data table. */
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/email";

            public static final int TYPE_HOME = 1;
            public static final int TYPE_WORK = 2;
            public static final int TYPE_OTHER = 3;
        }

        /**
         * Common data definition for postal addresses.
         */
        public static final class Postal implements BaseCommonColumns, CommonColumns, BaseTypes {
            private Postal() {}

            /** Mime-type used when storing this in data table. */
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/postal-address";

            /**
             * The MIME type of {@link #CONTENT_URI} providing a directory of
             * postal addresses.
             */
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/postal-address";

            /**
             * The content:// style URI for all data records of the
             * {@link Postal.CONTENT_ITEM_TYPE} mimetype, combined with the associated contact
             * and aggregate data.
             */
            public static final Uri CONTENT_URI = Uri.withAppendedPath(Data.CONTENT_URI,
                    "postals");

            public static final int TYPE_HOME = 1;
            public static final int TYPE_WORK = 2;
            public static final int TYPE_OTHER = 3;
        }

       /**
        * Common data definition for IM addresses.
        */
        public static final class Im implements BaseCommonColumns, CommonColumns, BaseTypes {
            private Im() {}

            /** Mime-type used when storing this in data table. */
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/im";

            public static final int TYPE_HOME = 1;
            public static final int TYPE_WORK = 2;
            public static final int TYPE_OTHER = 3;

            public static final String PROTOCOL = "data5";

            /**
             * The predefined IM protocol types. The protocol can either be non-present, one
             * of these types, or a free-form string. These cases are encoded in the PROTOCOL
             * column as:
             * <ul>
             * <li>null</li>
             * <li>pre:&lt;an integer, one of the protocols below&gt;</li>
             * <li>custom:&lt;a string&gt;</li>
             * </ul>
             */
            public static final int PROTOCOL_AIM = 0;
            public static final int PROTOCOL_MSN = 1;
            public static final int PROTOCOL_YAHOO = 2;
            public static final int PROTOCOL_SKYPE = 3;
            public static final int PROTOCOL_QQ = 4;
            public static final int PROTOCOL_GOOGLE_TALK = 5;
            public static final int PROTOCOL_ICQ = 6;
            public static final int PROTOCOL_JABBER = 7;

            public static String encodePredefinedImProtocol(int protocol) {
               return "pre:" + protocol;
            }

            public static String encodeCustomImProtocol(String protocolString) {
               return "custom:" + protocolString;
            }

            public static Object decodeImProtocol(String encodedString) {
               if (encodedString == null) {
                   return null;
               }

               if (encodedString.startsWith("pre:")) {
                   return Integer.parseInt(encodedString.substring(4));
               }

               if (encodedString.startsWith("custom:")) {
                   return encodedString.substring(7);
               }

               throw new IllegalArgumentException(
                       "the value is not a valid encoded protocol, " + encodedString);
            }
        }

        /**
         * Common data definition for organizations.
         */
        public static final class Organization implements BaseCommonColumns, BaseTypes {
            private Organization() {}

            /** Mime-type used when storing this in data table. */
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/organization";

            /**
             * The type of data, for example Home or Work.
             * <P>Type: INTEGER</P>
             */
            public static final String TYPE = "data1";

            public static final int TYPE_HOME = 1;
            public static final int TYPE_WORK = 2;
            public static final int TYPE_OTHER = 3;

            /**
             * The user provided label, only used if TYPE is {@link #TYPE_CUSTOM}.
             * <P>Type: TEXT</P>
             */
            public static final String LABEL = "data2";

            /**
             * The company as the user entered it.
             * <P>Type: TEXT</P>
             */
            public static final String COMPANY = "data3";

            /**
             * The position title at this company as the user entered it.
             * <P>Type: TEXT</P>
             */
            public static final String TITLE = "data4";
        }

        /**
         * Photo of the contact.
         */
        public static final class Photo implements BaseCommonColumns {
            private Photo() {}

            /** Mime-type used when storing this in data table. */
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/photo";

            /**
             * Thumbnail photo of the contact. This is the raw bytes of an image
             * that could be inflated using {@link BitmapFactory}.
             * <p>
             * Type: BLOB
             */
            public static final String PHOTO = "data1";
        }

        /**
         * Notes about the contact.
         */
        public static final class Note implements BaseCommonColumns {
            private Note() {}

            /** Mime-type used when storing this in data table. */
            public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/note";

            /**
             * The note text.
             * <P>Type: TEXT</P>
             */
            public static final String NOTE = "data1";
        }

        /**
         * Group Membership.
         */
        public static final class GroupMembership implements BaseCommonColumns {
            private GroupMembership() {}

            /** Mime-type used when storing this in data table. */
            public static final String CONTENT_ITEM_TYPE =
                    "vnd.android.cursor.item/group_membership";

            /**
             * The row id of the group that this group membership refers to. Either this or the
             * GROUP_SOURCE_ID must be set. If they are both set then they must refer to the same
             * group.
             * <P>Type: INTEGER</P>
             */
            public static final String GROUP_ROW_ID = "data1";

            /**
             * The source id of the group that this membership refers to. Either this or the
             * GROUP_ROW_ID must be set. If they are both set then they must refer to the same
             * group.
             * <P>Type: STRING</P>
             */
            public static final String GROUP_SOURCE_ID = "data2";
        }
    }

    public interface GroupsColumns {
        /**
         * The package name that owns this group.
         */
        public static final String PACKAGE = "package";

        /**
         * A unique identifier for the package that owns this group.
         */
        public static final String PACKAGE_ID = "package_id";

        /**
         * The display title of this group.
         * <p>
         * Type: TEXT
         */
        public static final String TITLE = "title";

        /**
         * The display title of this group to load as a resource from
         * {@link #PACKAGE}, which may be localized.
         * <p>
         * Type: TEXT
         */
        public static final String TITLE_RESOURCE = "title_res";

        /**
         * The total number of {@link Aggregates} that have
         * {@link GroupMembership} in this group. Read-only value that is only
         * present when querying {@link Groups#CONTENT_SUMMARY_URI}.
         * <p>
         * Type: INTEGER
         */
        public static final String SUMMARY_COUNT = "summ_count";

        /**
         * The total number of {@link Aggregates} that have both
         * {@link GroupMembership} in this group, and also have phone numbers.
         * Read-only value that is only present when querying
         * {@link Groups#CONTENT_SUMMARY_URI}.
         * <p>
         * Type: INTEGER
         */
        public static final String SUMMARY_WITH_PHONES = "summ_phones";

        /**
         * A reference to the name of the account to which this data belongs
         */
        public static final String ACCOUNT_NAME = "account_name";

        /**
         * A reference to the type of the account to which this data belongs
         */
        public static final String ACCOUNT_TYPE = "account_type";

        /**
         * A string that uniquely identifies this contact to its source, which is referred to
         * by the {@link #ACCOUNT_NAME} and {@link #ACCOUNT_TYPE}
         */
        public static final String SOURCE_ID = "sourceid";

        /**
         * An integer that is updated whenever this contact or its data changes.
         */
        public static final String VERSION = "version";

        /**
         * Set to 1 whenever the version changes
         */
        public static final String DIRTY = "dirty";

        /**
         * Flag indicating if the contacts belonging to this group should be
         * visible in any user interface.
         * <p>
         * Type: INTEGER
         */
        public static final String GROUP_VISIBLE = "group_visible";
    }

    /**
     * Constants for the groups table.
     */
    public static final class Groups implements BaseColumns, GroupsColumns {
        /**
         * This utility class cannot be instantiated
         */
        private Groups()  {}

        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "groups");

        /**
         * The content:// style URI for this table joined with details data from
         * {@link Data} and {@link Presence}.
         */
        public static final Uri CONTENT_SUMMARY_URI = Uri.withAppendedPath(AUTHORITY_URI,
                "groups_summary");

        /**
         * The MIME type of a directory of groups.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/group";

        /**
         * The MIME type of a single group.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/group";
    }

    /**
     * Constants for the contact aggregation exceptions table, which contains
     * aggregation rules overriding those used by automatic aggregation.  This type only
     * supports query and update. Neither insert nor delete are supported.
     */
    public static final class AggregationExceptions implements BaseColumns {
        /**
         * This utility class cannot be instantiated
         */
        private AggregationExceptions() {}

        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(AUTHORITY_URI, "aggregation_exceptions");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of data.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/aggregation_exception";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of an aggregation exception
         */
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/aggregation_exception";

        /**
         * The type of exception: {@link #TYPE_KEEP_IN}, {@link #TYPE_KEEP_OUT} or
         * {@link #TYPE_AUTOMATIC}.
         *
         * <P>Type: INTEGER</P>
         */
        public static final String TYPE = "type";

        /**
         * Allows the provider to automatically decide whether the aggregate should include
         * a particular contact or not.
         */
        public static final int TYPE_AUTOMATIC = 0;

        /**
         * Makes sure that the specified contact is included in the specified aggregate.
         */
        public static final int TYPE_KEEP_IN = 1;

        /**
         * Makes sure that the specified contact is NOT included in the specified aggregate.
         */
        public static final int TYPE_KEEP_OUT = 2;

        /**
         * A reference to the {@link Aggregates#_ID} of the aggregate that the rule applies to.
         */
        public static final String AGGREGATE_ID = "aggregate_id";

        /**
         * A reference to the {@link android.provider.ContactsContract.Contacts#_ID} of the
         * contact that the rule applies to.
         */
        public static final String CONTACT_ID = "contact_id";
    }

    private interface RestrictionExceptionsColumns {
        /**
         * Package name of a specific data provider, which will be matched
         * against {@link Data#PACKAGE}.
         * <p>
         * Type: STRING
         */
        public static final String PACKAGE_PROVIDER = "package_provider";

        /**
         * Package name of a specific data client, which will be matched against
         * the incoming {@link android.os.Binder#getCallingUid()} to decide if
         * the caller can access values with {@link Data#IS_RESTRICTED} flags.
         * <p>
         * Type: STRING
         */
        public static final String PACKAGE_CLIENT = "package_client";

        /**
         * Flag indicating if {@link #PACKAGE_PROVIDER} allows
         * {@link #PACKAGE_CLIENT} to access restricted {@link Data} rows.
         * <p>
         * Type: INTEGER
         */
        public static final String ALLOW_ACCESS = "allow_access";
    }

    /**
     * Constants for {@link Data} restriction exceptions. Sync adapters who
     * insert restricted data can use this table to specify exceptions about
     * which additional packages can access that restricted data.You can only
     * modify rules for a {@link RestrictionExceptionsColumns#PACKAGE_PROVIDER}
     * that your {@link android.os.Binder#getCallingUid()} owns.
     */
    public static final class RestrictionExceptions implements RestrictionExceptionsColumns {
        /**
         * This utility class cannot be instantiated
         */
        private RestrictionExceptions() {}

        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI,
                "restriction_exceptions");
    }

    /**
     * Contains helper classes used to create or manage {@link android.content.Intent Intents}
     * that involve contacts.
     */
    public static final class Intents {
        /**
         * This is the intent that is fired when a search suggestion is clicked on.
         */
        public static final String SEARCH_SUGGESTION_CLICKED =
                "android.provider.Contacts.SEARCH_SUGGESTION_CLICKED";

        /**
         * This is the intent that is fired when a search suggestion for dialing a number
         * is clicked on.
         */
        public static final String SEARCH_SUGGESTION_DIAL_NUMBER_CLICKED =
                "android.provider.Contacts.SEARCH_SUGGESTION_DIAL_NUMBER_CLICKED";

        /**
         * This is the intent that is fired when a search suggestion for creating a contact
         * is clicked on.
         */
        public static final String SEARCH_SUGGESTION_CREATE_CONTACT_CLICKED =
                "android.provider.Contacts.SEARCH_SUGGESTION_CREATE_CONTACT_CLICKED";

        /**
         * Starts an Activity that lets the user pick a contact to attach an image to.
         * After picking the contact it launches the image cropper in face detection mode.
         */
        public static final String ATTACH_IMAGE =
                "com.android.contacts.action.ATTACH_IMAGE";

        /**
         * Takes as input a data URI with a mailto: or tel: scheme. If a single
         * contact exists with the given data it will be shown. If no contact
         * exists, a dialog will ask the user if they want to create a new
         * contact with the provided details filled in. If multiple contacts
         * share the data the user will be prompted to pick which contact they
         * want to view.
         * <p>
         * For <code>mailto:</code> URIs, the scheme specific portion must be a
         * raw email address, such as one built using
         * {@link Uri#fromParts(String, String, String)}.
         * <p>
         * For <code>tel:</code> URIs, the scheme specific portion is compared
         * to existing numbers using the standard caller ID lookup algorithm.
         * The number must be properly encoded, for example using
         * {@link Uri#fromParts(String, String, String)}.
         * <p>
         * Any extras from the {@link Insert} class will be passed along to the
         * create activity if there are no contacts to show.
         * <p>
         * Passing true for the {@link #EXTRA_FORCE_CREATE} extra will skip
         * prompting the user when the contact doesn't exist.
         */
        public static final String SHOW_OR_CREATE_CONTACT =
                "com.android.contacts.action.SHOW_OR_CREATE_CONTACT";

        /**
         * Used with {@link #SHOW_OR_CREATE_CONTACT} to force creating a new
         * contact if no matching contact found. Otherwise, default behavior is
         * to prompt user with dialog before creating.
         * <p>
         * Type: BOOLEAN
         */
        public static final String EXTRA_FORCE_CREATE =
                "com.android.contacts.action.FORCE_CREATE";

        /**
         * Used with {@link #SHOW_OR_CREATE_CONTACT} to specify an exact
         * description to be shown when prompting user about creating a new
         * contact.
         * <p>
         * Type: STRING
         */
        public static final String EXTRA_CREATE_DESCRIPTION =
            "com.android.contacts.action.CREATE_DESCRIPTION";

        /**
         * Intents related to the Contacts app UI.
         */
        public static final class UI {
            /**
             * The action for the default contacts list tab.
             */
            public static final String LIST_DEFAULT =
                    "com.android.contacts.action.LIST_DEFAULT";

            /**
             * The action for the contacts list tab.
             */
            public static final String LIST_GROUP_ACTION =
                    "com.android.contacts.action.LIST_GROUP";

            /**
             * When in LIST_GROUP_ACTION mode, this is the group to display.
             */
            public static final String GROUP_NAME_EXTRA_KEY = "com.android.contacts.extra.GROUP";

            /**
             * The action for the all contacts list tab.
             */
            public static final String LIST_ALL_CONTACTS_ACTION =
                    "com.android.contacts.action.LIST_ALL_CONTACTS";

            /**
             * The action for the contacts with phone numbers list tab.
             */
            public static final String LIST_CONTACTS_WITH_PHONES_ACTION =
                    "com.android.contacts.action.LIST_CONTACTS_WITH_PHONES";

            /**
             * The action for the starred contacts list tab.
             */
            public static final String LIST_STARRED_ACTION =
                    "com.android.contacts.action.LIST_STARRED";

            /**
             * The action for the frequent contacts list tab.
             */
            public static final String LIST_FREQUENT_ACTION =
                    "com.android.contacts.action.LIST_FREQUENT";

            /**
             * The action for the "strequent" contacts list tab. It first lists the starred
             * contacts in alphabetical order and then the frequent contacts in descending
             * order of the number of times they have been contacted.
             */
            public static final String LIST_STREQUENT_ACTION =
                    "com.android.contacts.action.LIST_STREQUENT";

            /**
             * A key for to be used as an intent extra to set the activity
             * title to a custom String value.
             */
            public static final String TITLE_EXTRA_KEY =
                "com.android.contacts.extra.TITLE_EXTRA";

            /**
             * Activity Action: Display a filtered list of contacts
             * <p>
             * Input: Extra field {@link #FILTER_TEXT_EXTRA_KEY} is the text to use for
             * filtering
             * <p>
             * Output: Nothing.
             */
            public static final String FILTER_CONTACTS_ACTION =
                "com.android.contacts.action.FILTER_CONTACTS";

            /**
             * Used as an int extra field in {@link #FILTER_CONTACTS_ACTION}
             * intents to supply the text on which to filter.
             */
            public static final String FILTER_TEXT_EXTRA_KEY =
                "com.android.contacts.extra.FILTER_TEXT";
        }

        /**
         * Convenience class that contains string constants used
         * to create contact {@link android.content.Intent Intents}.
         */
        public static final class Insert {
            /** The action code to use when adding a contact */
            public static final String ACTION = Intent.ACTION_INSERT;

            /**
             * If present, forces a bypass of quick insert mode.
             */
            public static final String FULL_MODE = "full_mode";

            /**
             * The extra field for the contact name.
             * <P>Type: String</P>
             */
            public static final String NAME = "name";

            // TODO add structured name values here.

            /**
             * The extra field for the contact phonetic name.
             * <P>Type: String</P>
             */
            public static final String PHONETIC_NAME = "phonetic_name";

            /**
             * The extra field for the contact company.
             * <P>Type: String</P>
             */
            public static final String COMPANY = "company";

            /**
             * The extra field for the contact job title.
             * <P>Type: String</P>
             */
            public static final String JOB_TITLE = "job_title";

            /**
             * The extra field for the contact notes.
             * <P>Type: String</P>
             */
            public static final String NOTES = "notes";

            /**
             * The extra field for the contact phone number.
             * <P>Type: String</P>
             */
            public static final String PHONE = "phone";

            /**
             * The extra field for the contact phone number type.
             * <P>Type: Either an integer value from
             * {@link android.provider.Contacts.PhonesColumns PhonesColumns},
             *  or a string specifying a custom label.</P>
             */
            public static final String PHONE_TYPE = "phone_type";

            /**
             * The extra field for the phone isprimary flag.
             * <P>Type: boolean</P>
             */
            public static final String PHONE_ISPRIMARY = "phone_isprimary";

            /**
             * The extra field for an optional second contact phone number.
             * <P>Type: String</P>
             */
            public static final String SECONDARY_PHONE = "secondary_phone";

            /**
             * The extra field for an optional second contact phone number type.
             * <P>Type: Either an integer value from
             * {@link android.provider.Contacts.PhonesColumns PhonesColumns},
             *  or a string specifying a custom label.</P>
             */
            public static final String SECONDARY_PHONE_TYPE = "secondary_phone_type";

            /**
             * The extra field for an optional third contact phone number.
             * <P>Type: String</P>
             */
            public static final String TERTIARY_PHONE = "tertiary_phone";

            /**
             * The extra field for an optional third contact phone number type.
             * <P>Type: Either an integer value from
             * {@link android.provider.Contacts.PhonesColumns PhonesColumns},
             *  or a string specifying a custom label.</P>
             */
            public static final String TERTIARY_PHONE_TYPE = "tertiary_phone_type";

            /**
             * The extra field for the contact email address.
             * <P>Type: String</P>
             */
            public static final String EMAIL = "email";

            /**
             * The extra field for the contact email type.
             * <P>Type: Either an integer value from
             * {@link android.provider.Contacts.ContactMethodsColumns ContactMethodsColumns}
             *  or a string specifying a custom label.</P>
             */
            public static final String EMAIL_TYPE = "email_type";

            /**
             * The extra field for the email isprimary flag.
             * <P>Type: boolean</P>
             */
            public static final String EMAIL_ISPRIMARY = "email_isprimary";

            /**
             * The extra field for an optional second contact email address.
             * <P>Type: String</P>
             */
            public static final String SECONDARY_EMAIL = "secondary_email";

            /**
             * The extra field for an optional second contact email type.
             * <P>Type: Either an integer value from
             * {@link android.provider.Contacts.ContactMethodsColumns ContactMethodsColumns}
             *  or a string specifying a custom label.</P>
             */
            public static final String SECONDARY_EMAIL_TYPE = "secondary_email_type";

            /**
             * The extra field for an optional third contact email address.
             * <P>Type: String</P>
             */
            public static final String TERTIARY_EMAIL = "tertiary_email";

            /**
             * The extra field for an optional third contact email type.
             * <P>Type: Either an integer value from
             * {@link android.provider.Contacts.ContactMethodsColumns ContactMethodsColumns}
             *  or a string specifying a custom label.</P>
             */
            public static final String TERTIARY_EMAIL_TYPE = "tertiary_email_type";

            /**
             * The extra field for the contact postal address.
             * <P>Type: String</P>
             */
            public static final String POSTAL = "postal";

            /**
             * The extra field for the contact postal address type.
             * <P>Type: Either an integer value from
             * {@link android.provider.Contacts.ContactMethodsColumns ContactMethodsColumns}
             *  or a string specifying a custom label.</P>
             */
            public static final String POSTAL_TYPE = "postal_type";

            /**
             * The extra field for the postal isprimary flag.
             * <P>Type: boolean</P>
             */
            public static final String POSTAL_ISPRIMARY = "postal_isprimary";

            /**
             * The extra field for an IM handle.
             * <P>Type: String</P>
             */
            public static final String IM_HANDLE = "im_handle";

            /**
             * The extra field for the IM protocol
             * <P>Type: the result of {@link Contacts.ContactMethods#encodePredefinedImProtocol}
             * or {@link Contacts.ContactMethods#encodeCustomImProtocol}.</P>
             */
            public static final String IM_PROTOCOL = "im_protocol";

            /**
             * The extra field for the IM isprimary flag.
             * <P>Type: boolean</P>
             */
            public static final String IM_ISPRIMARY = "im_isprimary";
        }
    }

}
