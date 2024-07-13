package sorter;

import java.util.Comparator;

import model.Event;

/**
 * Klasa implementująca interfejs Comparator, służąca do sortowania listy
 * wydarzeń alfabetycznie według kategorii.
 */
public class SortEventByCategory implements Comparator<Event>
{
	@Override
	public int compare(Event o1, Event o2)
	{
		if (o1.getCategory() == null || o2.getCategory() == null)
			return -1;
		return o1.getCategory().getName().compareTo(o2.getCategory().getName());
	}
}