import java.util.HashSet;
import java.util.Set;
import java.util.regex.*;


//Utility class for validating input address, names.
public class Validator {

    private static Set<String> checkSet = new HashSet<>();
    public static boolean isValidAddress(String address) {

        String pattern = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|([a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)*)):[0-9]{1,5}$";

        Pattern regex = Pattern.compile(pattern);

        Matcher matcher = regex.matcher(address);

        return matcher.matches();
    }
    public static boolean isValidName(String name) {

        String[] parts = name.split(":");

        if (parts.length != 2) {
            return false;
        }

        if (!isValidEmail(parts[0])) {
            return false;
        }

        String uniqueString = parts[1];
        if (checkSet.contains(uniqueString))
        {
            return false;
        }
        checkSet.add(uniqueString);
        return true;
    }

    private static boolean isValidEmail(String email) {

        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        Pattern regex = Pattern.compile(emailPattern);

        Matcher matcher = regex.matcher(email);

        return matcher.matches();
    }

    public static void main(String[] args) {
        String[] addresses = {
                "user@example.com:unique1",
                "john.doe@example.com:unique1!!...",
                "invalid_address",
                "invalid_email@",
                "artem@gmail.com:adsf",
                "artem.korniienko@city.ac.uk:dummyNodeForActiveMapping",
                "user@example.com:unique1"
        };

        for (String address : addresses) {
            System.out.println(address + " - " + isValidName(address));
        }
    }
}
