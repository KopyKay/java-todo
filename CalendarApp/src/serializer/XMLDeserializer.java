package serializer;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import model.Category;
import model.Contact;
import model.Event;

/**
 * Klasa odpowiedzialna za deserializację danych z formatu XML. Wykorzystuje
 * obiekt {@link java.beans.XMLDecoder} do odczytu list kategorii, wydarzeń i
 * kontaktów z pliku XML.
 * 
 * @see java.beans.XMLDecoder
 * @see serializer.XMLDataWrapper
 */
public class XMLDeserializer
{
	private final String DATA_PATH = "data/xml_files/data.xml";

	/**
	 * Metoda do deserializacji danych z pliku XML z domyślnej ścieżki, do obiektu
	 * {@link serializer.XMLDataWrapper}.
	 * 
	 * @return Obiekt {@link serializer.XMLDataWrapper} zawierający listy kategorii,
	 *         wydarzeń i kontaktów.
	 */
	public XMLDataWrapper decode()
	{
		return decode(DATA_PATH);
	}

	/**
	 * Metoda do deserializacji danych z określonego pliku XML do obiektu
	 * {@link serializer.XMLDataWrapper}.
	 * 
	 * @param  filePath Ścieżka do pliku XML.
	 * @return          Obiekt {@link serializer.XMLDataWrapper} zawierający listy
	 *                  kategorii, wydarzeń i kontaktów.
	 */
	@SuppressWarnings("unchecked")
	public XMLDataWrapper decode(String filePath)
	{
		File file = new File(DATA_PATH);

		if (file.length() == 0)
			return null;

		try (XMLDecoder xmlDecoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(filePath))))
		{
			List<Category> categories = (List<Category>) xmlDecoder.readObject();
			List<Event> events = (List<Event>) xmlDecoder.readObject();
			List<Contact> contacts = (List<Contact>) xmlDecoder.readObject();

			return new XMLDataWrapper(categories, events, contacts);
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
			return null;
		}
	}
}
