package sorter;

import java.util.Comparator;

import model.Contact;

/**
 * Klasa implementująca interfejs Comparator, służąca do sortowania listy
 * kontaktów alfabetycznie według nazwiska.
 */
public class SortContactByLastName implements Comparator<Contact>
{
	@Override
	public int compare(Contact o1, Contact o2)
	{
		return o1.getLastName().compareTo(o2.getLastName());
	}
}
