package sorter;

import java.util.Comparator;

import model.Event;

/**
 * Klasa implementująca interfejs Comparator, służąca do sortowania listy
 * wydarzeń alfabetycznie według lokalizacji.
 */
public class SortEventByLocation implements Comparator<Event>
{
	@Override
	public int compare(Event o1, Event o2)
	{
		return o1.getLocation().compareTo(o2.getLocation());
	}
}
