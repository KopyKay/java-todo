package sorter;

import java.util.Comparator;

import model.Event;

/**
 * Klasa implementująca interfejs Comparator, służąca do sortowania listy
 * wydarzeń alfabetycznie według opisu.
 */
public class SortEventByDescription implements Comparator<Event>
{
	@Override
	public int compare(Event o1, Event o2)
	{
		return o1.getDescription().compareTo(o2.getDescription());
	}
}
