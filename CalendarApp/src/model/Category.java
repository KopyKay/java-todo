package model;

/**
 * Reprezentuje kategorię w kalendarzu. Posiada identyfikator, nazwę oraz kod
 * koloru w formie heksadecymalnej. Implementuje interfejs Comparable,
 * umożliwiając sortowanie kategorii po nazwie.
 */
public class Category implements Comparable<Category>
{
	private int id;
	private String name;
	private String colorHex;

	/**
	 * Pusty konstuktor klasy {@link Category}, potrzebny na rzecz serializacji
	 * danych.
	 */
	public Category()
	{

	}

	/**
	 * Zwraca tekstową reprezentację obiektu {@link model.Category}, włączając imię,
	 * nazwisko i numer telefonu.
	 *
	 * @return Tekstowa reprezentacja kategorii.
	 */
	@Override
	public String toString()
	{
		return name;
	}

	/**
	 * @return Identyfikator kategorii.
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Ustawia identyfikator kategorii.
	 *
	 * @param id Nowy identyfikator kategorii.
	 */
	public void setId(int id)
	{
		this.id = id;
	}

	/**
	 * @return Nazwa kategorii.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Ustawia nazwę kategorii.
	 *
	 * @param name Nowa nazwa kategorii.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return Kod koloru kategorii w formie heksadecymalnej.
	 */
	public String getColorHex()
	{
		return colorHex;
	}

	/**
	 * Ustawia kod koloru kategorii w formie heksadecymalnej.
	 *
	 * @param colorHex Nowy kod koloru kategorii.
	 */
	public void setColorHex(String colorHex)
	{
		this.colorHex = colorHex;
	}

	@Override
	public int compareTo(Category o)
	{
		return this.name.compareTo(o.getName());
	}
}
