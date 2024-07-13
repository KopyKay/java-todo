package serializer;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.time.LocalDateTime;

/**
 * Klasa {@link serializer.LocalDateTimePersistenceDelegate} dostarcza
 * niestandardowe trwałości przechowywania danych dla obiektów klasy
 * {@link java.time.LocalDateTime} podczas procesu serializacji do formatu XML.
 * W przypadku, gdy obiekt klasy {@code LocalDateTime} jest zapisywany do pliku
 * XML, domyślny mechanizm serializacji może nie uwzględniać specyficznych
 * formatów reprezentacji daty i czasu. Ta klasa została stworzona, aby
 * skonfigurować sposób zapisu i odczytu obiektów {@code LocalDateTime}. Kiedy
 * proces serializacji napotyka obiekt klasy {@code LocalDateTime}, ta klasa
 * przekazuje odpowiednie informacje do mechanizmu kodującego XML. W wyniku
 * tego, przy odczycie danych z pliku XML, możliwe jest prawidłowe odtworzenie
 * obiektu {@code LocalDateTime} z uwzględnieniem pierwotnego formatu daty i
 * czasu.
 */
public class LocalDateTimePersistenceDelegate extends DefaultPersistenceDelegate
{
	@Override
	protected Expression instantiate(Object oldInstance, Encoder out)
	{
		LocalDateTime ldt = (LocalDateTime) oldInstance;
		return new Expression(ldt, oldInstance.getClass(), "parse", new Object[] { ldt.toString() });
	}
}
