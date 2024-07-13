package serializer;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.time.LocalTime;

/**
 * Klasa {@link serializer.LocalTimePersistenceDelegate} dostarcza
 * niestandardowe trwałości przechowywania danych dla obiektów klasy
 * {@link java.time.LocalTime} podczas procesu serializacji do formatu XML. W
 * przypadku, gdy obiekt klasy {@code LocalTime} jest zapisywany do pliku XML,
 * domyślny mechanizm serializacji może nie uwzględniać specyficznych formatów
 * reprezentacji czasu. Ta klasa została stworzona, aby skonfigurować sposób
 * zapisu i odczytu obiektów {@code LocalTime}. Kiedy proces serializacji
 * napotyka obiekt klasy {@code LocalTime}, ta klasa przekazuje odpowiednie
 * informacje do mechanizmu kodującego XML. W wyniku tego, przy odczycie danych
 * z pliku XML, możliwe jest prawidłowe odtworzenie obiektu {@code LocalTime} z
 * uwzględnieniem pierwotnego formatu czasu.
 */
public class LocalTimePersistenceDelegate extends DefaultPersistenceDelegate
{
	@Override
	protected Expression instantiate(Object oldInstance, Encoder out)
	{
		LocalTime lt = (LocalTime) oldInstance;
		return new Expression(lt, oldInstance.getClass(), "parse", new Object[] { lt.toString() });
	}
}
