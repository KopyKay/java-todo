package serializer;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import model.Category;
import model.Contact;
import model.Event;

/**
 * Klasa odpowiedzialna za serializację danych do formatu XML. Wykorzystuje
 * obiekt {@link java.beans.XMLEncoder} do zapisu list kategorii, wydarzeń i
 * kontaktów do pliku XML.
 * 
 * @see java.beans.XMLEncoder
 * @see serializer.LocalDateTimePersistenceDelegate
 * @see serializer.LocalTimePersistenceDelegate
 */
public class XMLSerializer
{
	private final String DATA_PATH = "data/xml_files/data.xml";

	/**
	 * Serializuje listę kategorii, wydarzeń i kontaktów do pliku XML.
	 * 
	 * @param categories Lista kategorii do zapisania.
	 * @param events     Lista wydarzeń do zapisania.
	 * @param contacts   Lista kontaktów do zapisania.
	 */
	public void encode(List<Category> categories, List<Event> events, List<Contact> contacts)
	{
		isFileExists();

		try (XMLEncoder xmlEncoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(DATA_PATH))))
		{
			xmlEncoder.setPersistenceDelegate(LocalDateTime.class, new LocalDateTimePersistenceDelegate());
			xmlEncoder.setPersistenceDelegate(LocalTime.class, new LocalTimePersistenceDelegate());

			xmlEncoder.writeObject(categories);
			xmlEncoder.writeObject(events);
			xmlEncoder.writeObject(contacts);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Sprawdza istnienie pliku danych. Jeśli plik nie istnieje, próbuje go
	 * utworzyć.
	 */
	private void isFileExists()
	{
		File dataFile = new File(DATA_PATH);

		if (!dataFile.exists())
		{
			try
			{
				if (dataFile.getParentFile() != null && !dataFile.getParentFile().exists())
				{
					dataFile.getParentFile().mkdirs();
				}

				dataFile.createNewFile();
			}
			catch (IOException e)
			{
				System.err.println(e.getMessage());
			}
		}
	}
}
