package sorter;

import java.util.Comparator;

import model.Contact;

/**
 * Klasa implementująca interfejs Comparator, służąca do sortowania listy
 * kontaktów według numerów telefonów.
 */
public class SortContactByPhoneNumber implements Comparator<Contact>
{
	@Override
	public int compare(Contact o1, Contact o2)
	{
		return o1.getPhoneNumber().compareTo(o2.getPhoneNumber());
	}
}
