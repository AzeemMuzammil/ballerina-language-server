type ContactDetails record {|
    SecondaryPhonesX[] phoneNumbers?;
    string[] addresses?;
|};

type Info record {|
    SecondaryPhones[] secondaryPhones;
    string[] emails;
    string[][] addresses;
|};

type SecondaryPhones record {|
    string code;
    string number;
|};

type User record {|
    Info info;
|};

type Person record {|
    ContactDetails contactDetails;
|};

type SecondaryPhonesX record {|
    string code;
    string number;
|};

public function main() {
    Info info = {secondaryPhones: [], emails: [], addresses: []};
    SecondaryPhones[] newSecondaryPhones = info.secondaryPhones;
    Person p = {
           contactDetails: {
               phoneNumbers: let string num = "10" in from var secondaryPhonesItem in newSecondaryPhones
                   select {code: secondaryPhonesItem.code, number: num}
           }
       };
}