package view;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;

import application.Controller;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import model.Event;

/**
 * Klasa reprezentująca widok kalendarza w aplikacji. Pozwala na przeglądanie
 * wydarzeń zorganizowanych według daty i podglądanie ich szczegółów. Wyświetla
 * datę i zsynchronizowany czas z aktualnym czasem lokalnym. Implementuje
 * graficzny interfejs użytkownika przy użyciu JavaFX.
 */
public class CalendarView
{
	private Controller controller;
	private DateTimeFormatter dateTimeFormatter;
	private LocalDateTime dateFocus;
	private LocalDateTime today;
	private Timeline timeline;
	private Alert alert;

	private List<Event> eventList;

	@FXML
	private ComboBox<String> comboBox_Month;

	@FXML
	private ComboBox<String> comboBox_Year;

	@FXML
	private FlowPane flowPane_Calendar;

	@FXML
	private Text text_DateTime;

	/**
	 * Obsługuję zdarzenie kliknięcia przycisku "Next". Przesuwa skoncentrowaną datę
	 * o jeden miesiąc do przodu i odświeża kalendarz.
	 */
	@FXML
	private void button_NextMonth_Click(ActionEvent actionEvent)
	{
		this.dateFocus = dateFocus.plusMonths(1);
		flowPane_Calendar.getChildren().clear();
		refreshCalendar();
	}

	/**
	 * Obsługuję zdarzenie kliknięcia przycisku "Previous". Przesuwa skoncentrowaną
	 * datę o jeden miesiąc do tyłu i odświeża kalendarz.
	 */
	@FXML
	private void button_PreviousMonth_Click(ActionEvent actionEvent)
	{
		this.dateFocus = dateFocus.minusMonths(1);
		flowPane_Calendar.getChildren().clear();
		refreshCalendar();
	}

	/**
	 * Obsługuję zdarzenie wyboru miesiąca z ComboBox. Ustawia skoncentrowaną datę
	 * na wybrany miesiąc i odświeża kalendarz.
	 */
	@FXML
	private void comboBox_Month_Select(ActionEvent actionEvent)
	{
		String selectedMonth = comboBox_Month.getValue();
		Month month = Month.valueOf(selectedMonth);
		this.dateFocus = dateFocus.withMonth(month.getValue());
		refreshCalendar();
	}

	/**
	 * Obsługuję zdarzenie wyboru roku z ComboBox. Ustawia skoncentrowaną datę na
	 * wybrany rok i odświeża kalendarz.
	 */
	@FXML
	private void comboBox_Year_Select(ActionEvent actionEvent)
	{
		String selectedYear = comboBox_Year.getValue();
		int year = Integer.parseInt(selectedYear);
		this.dateFocus = dateFocus.withYear(year);
		refreshCalendar();
	}

	/**
	 * Tworzy niestandardowy StackPane jako komórkę, która reprezentuje pojedynczy
	 * dzień w kalendarzu wraz z istniejącymi danego dnia wydarzeniami.
	 *
	 * @param  day Numer dnia, który ma zostać wyświetlony w komórce kalendarza.
	 * @return     StackPane reprezentujący dzień z odpowiednimi informacjami i
	 *             obsługą zdarzeń.
	 */
	private StackPane createDayStackPane(int day)
	{
		// Stworzenie obiektu StackPane jako kontener dla poniższego kwadratu
		StackPane stackPane = new StackPane();

		// Tworzenie obiektu prostokąta reprezentującego komórkę dla danego dnia
		Rectangle rectangle = new Rectangle();
		double cellWidth = (flowPane_Calendar.getPrefWidth() - flowPane_Calendar.getHgap()) / 7;
		double cellHeight = (flowPane_Calendar.getPrefHeight() - flowPane_Calendar.getVgap()) / 6;

		double rectangleWidth = cellWidth - 10;
		double rectangleHeight = cellHeight - 10;

		// Ustawienie właściwości prostokąta
		rectangle.setFill(Color.TRANSPARENT);
		rectangle.setStroke(Color.BLACK);
		rectangle.setStrokeWidth(1);
		rectangle.setWidth(rectangleWidth);
		rectangle.setHeight(rectangleHeight);

		// Dodanie prostokąta do StackPane
		stackPane.getChildren().add(rectangle);

		// Wyświetlenie numeru dnia
		Text date = new Text(String.valueOf(day));
		double textTranslationY = -(rectangleHeight / 2) * 0.75;

		// Dodanie tekstu do StackPane
		date.setTranslateY(textTranslationY);
		stackPane.getChildren().add(date);

		// Podświetlenie dzisiejszej daty czerwonym obramowaniem
		if (this.today.getYear() == this.dateFocus.getYear() && this.today.getMonth() == this.dateFocus.getMonth()
				&& this.today.getDayOfMonth() == day)
		{
			rectangle.setStroke(Color.RED);
		}

		// Ustawienie odstępów wewnętrznych dla StackPane
		stackPane.setPadding(new Insets(5, 5, 5, 5));

		// Pobieranie wydarzeń dla danego dnia
		List<Event> eventsForDay = this.controller.getEventsByDate(this.dateFocus.toLocalDate().withDayOfMonth(day));

		// Jeśli są wydarzenia danego dnia -> pokaż
		if (!eventsForDay.isEmpty())
		{
			VBox eventsContainer = new VBox();
			eventsContainer.setAlignment(Pos.BOTTOM_CENTER);

			// Wyświetlenie maksymalnie 2 wydarzeń, Math.min ponieważ może być danego dnia
			// jeden event, a w przypadku więszkej ilości wybierz tylko dwa
			int maxEventsToShow = Math.min(2, eventsForDay.size());

			for (int i = 0; i < maxEventsToShow; i++)
			{
				Event event = eventsForDay.get(i);

				// Tworzenie tekstowej reprezentacji wydarzenia, jeżeli nazwa jest dłuższa niż
				// 12 znaków to utnij do 9 znaków i dodaj ".." na koniec
				Text eventText = new Text();
				String eventName = event.getName().length() > 12 ? event.getName().substring(0, 9) + ".." : event.getName();
				eventText.setText(eventName);

				// Tworzenie tła dla tekstu wydarzenia
				Bounds textBounds = eventText.getBoundsInLocal();
				Rectangle backgroundRect = new Rectangle(textBounds.getWidth(), textBounds.getHeight() - 4);

				// Ustawienie koloru tła na podstawie kategorii wydarzenia
				if (event.getCategory() != null && event.getCategory().getColorHex() != null)
				{
					String colorHex = event.getCategory().getColorHex();
					backgroundRect.setFill(Color.web(colorHex));
				}
				else
				{
					backgroundRect.setFill(Color.TRANSPARENT);
				}

				// Połączeniu tekstu i tła wydarzenia w StackPane
				StackPane eventStackPane = new StackPane(backgroundRect, eventText);
				eventsContainer.getChildren().add(eventStackPane);
			}

			// Jeśli jest więcej wydarzeń niż wyświetlanych, pokazuje tekst "more..."
			if (eventsForDay.size() > maxEventsToShow)
			{
				Text moreText = new Text("more...");
				eventsContainer.getChildren().add(moreText);
			}

			// Dodanie wydarzeń do VBox
			stackPane.getChildren().add(eventsContainer);

			// Ustawianie obsługi zdarzenia dla kliknięcia myszą w celu wyświetlenia
			// szczegółów wydarzenia w oknie Alert
			stackPane.setOnMouseClicked(e ->
			{
				Alert alert = new Alert(AlertType.INFORMATION);

				StringBuilder eventDetails = new StringBuilder();
				for (Event dayEvent : eventsForDay)
				{
					eventDetails.append(String.format("%s %02d:%02d %s%n%s%n%s%nNotify at: %s%n%n", dayEvent.getName(), dayEvent.getDate().getHour(),
							dayEvent.getDate().getMinute(), dayEvent.getLocation(), dayEvent.getDescription(), dayEvent.getContacts(),
							dayEvent.getFormattedDateWithOffset()));
				}

				alert.setHeaderText(
						"Events for date: " + dateFocus.withDayOfMonth(day).toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
				alert.setContentText(eventDetails.toString());

				alert.showAndWait();
			});
		}

		return stackPane;
	}

	/**
	 * Odświeża widok kalendarza, aktualizując jego zawartość w zależności od
	 * bieżącej daty. Aktualizuje wyświetlane miesiące i lata w kontrolkach
	 * ComboBox, tworzy nowy GridPane z komórkami reprezentującymi rozstawienie dni
	 * w danej dacie oraz dodaje go do kontenera flowPane_Calendar.
	 */
	public void refreshCalendar()
	{
		comboBox_Month.setValue(String.valueOf(this.dateFocus.getMonth()));
		comboBox_Year.setValue(String.valueOf(this.dateFocus.getYear()));

		// Maksymalna liczba dni w bieżącym miesiącu
		int monthMaxDate = this.dateFocus.getMonth().maxLength();

		// Poprawka dla lutego w przypadku nieprzestępnego roku
		if (this.dateFocus.getYear() % 4 != 0 && monthMaxDate == 29)
		{
			monthMaxDate = 28;
		}

		// Określenie przesunięcia dni tygodnia dla pierwszego dnia miesiąca
		int dateOffset = LocalDateTime.of(dateFocus.getYear(), dateFocus.getMonthValue(), 1, 0, 0).getDayOfWeek().getValue();

		// Dostosowanie dateOffset, aby zaczynał się od poniedziałku (1 = poniedziałek,
		// 2 = wtorek, ..., 7 = niedziela)
		// Operacja (dateOffset + 5) % 7 + 1 przekształca wynik z getDayOfWeek(),
		// tak aby liczby odpowiadały standardowemu układowi tygodnia, gdzie
		// poniedziałek zaczyna się od 1.
		// (dateOffset + 5): Ta operacja przesuwa dni tygodnia o 5 pozycji.
		// (dateOffset + 5) % 7): Operacja ta normalizuje przesuniętą wartość do
		// przedziału od 0 do 6
		// (dateOffset + 5) % 7 + 1): Na końcu dodajemy 1, aby doprowadzić do pełnego
		// dostosowania wartości tak, że poniedziałek ma teraz wartość 1, wtorek 2, ...,
		// niedziela 7.
		dateOffset = (dateOffset + 5) % 7 + 1;

		GridPane gridPane = new GridPane();
		gridPane.setHgap(flowPane_Calendar.getHgap());
		gridPane.setVgap(flowPane_Calendar.getVgap());

		// Iteracja po siatce kalendarza (6 wierszy x 7 kolumn)
		for (int i = 0; i < 6; i++)
		{
			for (int j = 0; j < 7; j++)
			{
				int calculatedDate = (j + 1) + (7 * i);

				// Sprawdzenie, czy obliczony dzień jest już poza przesunięciem
				if (calculatedDate > dateOffset)
				{
					int currentDate = calculatedDate - dateOffset;

					// Sprawdzenie, czy bieżący dzień mieści się w zakresie bieżącego miesiąca
					if (currentDate <= monthMaxDate)
					{
						// Utworzenie niestandardowego StackPane reprezentującego dzień i dodanie go do
						// GridPane
						StackPane stackPane = createDayStackPane(currentDate);
						gridPane.add(stackPane, j, i);
					}
				}
			}
		}

		// Wyczyszczenie istniejącej zawartości flowPane_Calendar i dodanie do niego
		// nowego GridPane
		flowPane_Calendar.getChildren().clear();
		flowPane_Calendar.getChildren().add(gridPane);
	}

	/**
	 * Sprawdza czy dla aktualnej daty i czasu są powiadomienia do wyświetlenia. W
	 * przypadku znalezienia pasującego wydarzenia, wyświetla okno dialogowe z
	 * informacją o wydarzeniu.
	 */
	private void checkDate()
	{
		LocalDateTime runningDateTime = LocalDateTime.parse(text_DateTime.getText(), this.dateTimeFormatter);

		for (Event event : this.eventList)
		{
			if (event.getDateWithOffset().equals(runningDateTime))
			{
				alert.setTitle("Notification");
				alert.setHeaderText(event.getName() + " | " + event.getLocation() + " | " + event.getCategory());
				alert.setContentText(event.getDescription());
				alert.showAndWait();
			}
		}
	}

	/**
	 * Aktualizuje wyświetlaną datę i sprawdza, czy są powiadomienia do
	 * wyświetlenia.
	 */
	private void updateDateTime()
	{
		text_DateTime.setText(LocalDateTime.now().format(dateTimeFormatter));
		checkDate();
	}

	/**
	 * Uruchamia cykliczne odświeżanie daty i sprawdzanie powiadomień z
	 * wykorzystaniem Timeline i KeyFrame. Uruchamiane przy inicjalizacji widoku, a
	 * odświeżanie w głównym wątku.
	 */
	private void startDateTime()
	{
		timeline = new Timeline(new KeyFrame(Duration.seconds(1), event ->
		{
			Platform.runLater(() -> updateDateTime());
		}));
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();
	}

	/**
	 * Inicjalizuje widok kalendarza, ustawiając referencję do kontrolera,
	 * pobierając listę wydarzeń oraz ustawiając początkowe wartości daty.
	 * Inicjalizuje ComboBoxy z miesiącami i latami, rozpoczyna cykliczne
	 * odświeżanie daty i inicjalizuje kalendarz.
	 *
	 * @param controller Referencja do kontrolera aplikacji.
	 */
	public void init(Controller controller)
	{
		this.controller = controller;
		this.eventList = this.controller.getEvents();
		this.alert = new Alert(AlertType.INFORMATION);

		this.dateFocus = LocalDateTime.now();
		this.today = LocalDateTime.now();
		this.dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

		for (Month month : Month.values())
		{
			comboBox_Month.getItems().add(month.toString());
		}

		for (int year = 2000; year <= 2100; year++)
		{
			comboBox_Year.getItems().add(String.valueOf(year));
		}

		updateDateTime();
		startDateTime();
		refreshCalendar();
	}
}
