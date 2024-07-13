package sorter;

import java.util.Comparator;

import model.Event;

/**
 * Klasa implementująca interfejs Comparator, służąca do sortowania listy
 * wydarzeń chronologicznie według daty.
 */
public class SortEventByDate implements Comparator<Event>
{
	@Override
	public int compare(Event o1, Event o2)
	{
		return o1.getDate().compareTo(o2.getDate());
	}
}
