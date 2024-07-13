package sorter;

import java.util.Comparator;

import model.Event;

/**
 * Klasa implementująca interfejs Comparator, służąca do sortowania listy
 * wydarzeń alfabetycznie według nazwy.
 */
public class SortEventByName implements Comparator<Event>
{
	@Override
	public int compare(Event o1, Event o2)
	{
		return o1.getName().compareTo(o2.getName());
	}
}