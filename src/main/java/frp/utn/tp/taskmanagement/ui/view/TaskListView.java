package frp.utn.tp.taskmanagement.ui.view;

import frp.utn.tp.base.ui.component.ViewToolbar;
import frp.utn.tp.taskmanagement.domain.Person;
import frp.utn.tp.taskmanagement.domain.Task;
import frp.utn.tp.taskmanagement.service.PersonService;
import frp.utn.tp.taskmanagement.service.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("task-list")
@PageTitle("Lista de Tareas")
@Menu(order = 1, icon = "vaadin:tasks", title = "Lista de Tareas")
@PermitAll
public class TaskListView extends Main implements BeforeEnterObserver {

    private final TaskService taskService;
    private final PersonService personService;

    final TextField description;
    final ComboBox<Person> personCombo; // Este es tu ComboBox de filtro de personas
    final DatePicker dueDate;
    final Button createBtn;
    final Button allTasksBtn;
    final Grid<Task> taskGrid;

    public TaskListView(TaskService taskService, PersonService personService, Clock clock) {
        this.taskService = taskService;
        this.personService = personService;

        description = new TextField("Tarea");
        description.setPlaceholder("Que deseas hacer?");
        description.setAriaLabel("Descripción");
        description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
        description.setMinWidth("20em");

        personCombo = new ComboBox<Person>("Filtrar tareas por persona...");
        personCombo.setWidth("200px");
        personCombo.setItems(query -> personService.list(toSpringPageRequest(query)).stream());
        personCombo.setItemLabelGenerator( person -> {
                if(person == null) return "Todas las Tareas"; // Texto cuando no hay persona seleccionada
                return person.toString();
        });
        personCombo.addValueChangeListener(event -> {
            cargarTasks(); // Se llama a cargarTasks cada vez que el valor del ComboBox cambia
        });
        
        dueDate = new DatePicker("Fecha Límite");
        dueDate.setPlaceholder("dd/mm/aaaa");
        dueDate.setAriaLabel("Fecha Límite");

        HorizontalLayout formTask = new HorizontalLayout();
        formTask.add(description,personCombo,dueDate);

        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(clock.getZone())
                .withLocale(getLocale());
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());

        HorizontalLayout buttons = new HorizontalLayout();

        createBtn = new Button("Crear", new Icon(VaadinIcon.PLUS_CIRCLE) ,event -> createTask());
        createBtn.setWidth("100px");
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        allTasksBtn = new Button("Ver Todas las Tareas", new Icon(VaadinIcon.CLIPBOARD_TEXT) ,
                event -> {
                        personCombo.setValue(null); // Establece el ComboBox a nulo
                        //Llama a cargarTasks() explícitamente para asegurar la recarga
                        cargarTasks(); 
                });
        allTasksBtn.setWidth("200px");
        allTasksBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        buttons.add(createBtn, allTasksBtn);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        taskGrid = new Grid<>();
        
        // La carga inicial de datos se manejará en beforeEnter para considerar los parámetros de URL.
                
        taskGrid.addComponentColumn(task -> {
                Checkbox checkbox = new Checkbox(task.isDone());
                checkbox.addValueChangeListener(event -> {
                        task.setDone(event.getValue());
                        taskService.updateTask(task);
                });
                return checkbox;
        }).setHeader("Completada");
        taskGrid.addColumn(Task::getDescription).setHeader("Descripción");
        taskGrid.addColumn(Task::getPerson).setHeader("Persona");
        taskGrid.addColumn(task -> Optional.ofNullable(task.getDueDate()).map(dateFormatter::format).orElse("Nunca"))
                .setHeader("Fecha Límite");
        taskGrid.addColumn(task -> dateTimeFormatter.format(task.getCreationDate())).setHeader("Fecha de Creación");
        taskGrid.addComponentColumn(task -> {
                Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), event ->{
                        Dialog dialog = new Dialog();

                        dialog.setHeaderTitle(String.format("Tarea: \"%s\"",task.getDescription()));
                        dialog.add("¿Estas seguro que deseas borrar esta tarea?");

                        Button deleteDlgButton = new Button("Borrar", (e) -> {
                                taskService.deleteTask(task.getId());
                                cargarTasks(); // Refresca la grilla después de eliminar
                                dialog.close();
                        });
                        deleteDlgButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
                        deleteDlgButton.getStyle().set("margin-right", "auto");
                        dialog.getFooter().add(deleteDlgButton);

                        Button cancelButton = new Button("Cancelar", (e) -> dialog.close());
                        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                        dialog.getFooter().add(cancelButton);

                        dialog.open();
                });
                deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON);
                deleteButton.setAriaLabel("Borrar");
                deleteButton.setTooltipText("Eliminar");
                return deleteButton;
        }).setHeader("Borrar");

        taskGrid.setSizeFull();

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("Tareas"));
        add(formTask);
        add(buttons);
        add(taskGrid);
    }

    private void createTask() {
        taskService.createTask(description.getValue(), personCombo.getValue(), dueDate.getValue());
        cargarTasks();
        description.clear();
        dueDate.clear();
        Notification.show("Tarea Agregada", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    
    // Este método es clave para la lógica de filtrado/mostrar todas
    private void cargarTasks(){
        Person person = personCombo.getValue(); // Obtiene la persona seleccionada en el ComboBox
        if( person != null) {
            taskGrid.setItems(taskService.findByPerson(person));
        } else {
            taskGrid.setItems(query -> taskService.list(toSpringPageRequest(query)).stream());
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        Map<String, List<String>> parametersMap = queryParameters.getParameters();

        if (parametersMap.containsKey("personId")) {
            List<String> personIds = parametersMap.get("personId");
            if (!personIds.isEmpty()) {
                try {
                    Long personId = Long.parseLong(personIds.get(0));
                    Optional<Person> personOptional = personService.findById(personId);

                    if (personOptional.isPresent()) {
                        Person personToFilter = personOptional.get();
                        // Al establecer el valor en el ComboBox, su ValueChangeListener llamará a cargarTasks().
                        personCombo.setValue(personToFilter);
                    } else {
                        Notification.show("Persona no encontrada con ID: " + personId, 3000, Notification.Position.BOTTOM_START)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        // Si la persona no existe, se aseguran que se muestren todas las tareas
                        personCombo.setValue(null); // Esto también llamará a cargarTasks()
                    }
                } catch (NumberFormatException e) {
                    Notification.show("ID de persona inválido.", 3000, Notification.Position.BOTTOM_START)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    // Si el ID es inválido, se aseguran que se muestren todas las tareas
                    personCombo.setValue(null); // Esto también llamará a cargarTasks()
                }
            }
        } else {
            // Si NO hay parámetro personId en la URL al cargar la vista,
            // nos aseguramos de que el ComboBox no tenga una persona seleccionada.
            // Esto provocará que cargarTasks() se ejecute y muestre TODAS las tareas por defecto.
            personCombo.setValue(null); 
        }
    }
}