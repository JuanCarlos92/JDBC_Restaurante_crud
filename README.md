# Sistema de Gestión de Pedidos de Restaurante

Este proyecto permite gestionar los pedidos de un restaurante, incluyendo el registro de clientes, productos, pedidos y generación de informes de ventas.

## Funcionalidades Principales

### Gestión de Clientes
- Registrar clientes con nombre, teléfono y dirección.
- Buscar, actualizar y eliminar clientes.

### Gestión de Productos
- Registrar productos del menú con nombre, categoría, precio y disponibilidad.
- Modificar y eliminar productos.

### Gestión de Pedidos
- Crear pedidos con cliente, productos seleccionados, cantidad y total.
- Modificar y eliminar pedidos.
- Controlar estado del pedido (pendiente, en preparación, entregado).

### Generación de Informes con JasperReports
- Informe de ventas diarias y mensuales.
- Informe de productos más vendidos.
- Informe de pedidos pendientes.

## Interfaz con JavaFX y Scene Builder

- **Menú Principal** → Para navegar entre módulos (Clientes, Productos, Pedidos).
- **Gestión de Clientes** → Formulario para agregar, editar, eliminar y listar clientes.
- **Gestión de Productos** → Formulario para administrar el menú del restaurante.
- **Gestión de Pedidos** → Selección de cliente, productos y confirmación del pedido.

---

## Plan de Desarrollo

### Primera entrega
- Crear la Base de Datos en MySQL.
- Configurar la conexión entre Java y MySQL.
- Diseñar el menú principal y formulario de gestión de clientes.
- Programar las funciones CRUD para Clientes.
- Mostrar los clientes en una TableView.

### Segunda entrega
- Diseñar el formulario para la gestión de Productos.
- Programar las funciones CRUD para Productos.
- Conectar la base de datos para mostrar productos en una TableView.
- Implementar transiciones entre ventanas.
- Agregar botón para volver a la ventana principal.

### Tercera entrega
- Diseñar el formulario para la gestión de Pedidos.
- Programar las funciones CRUD para Pedidos.
- Crear tablas para almacenar pedidos y detalles de pedidos.
- Conectar la base de datos y mostrar pedidos en una TableView.
- Implementar vista detallada de pedidos con productos asociados.
- Agregar transiciones entre ventanas y botón para volver a la ventana principal.

---

## Tecnologías Utilizadas
- **Java** con **JavaFX** y **Scene Builder**
- **MySQL** para la base de datos
- **JasperReports** para la generación de informes

## Requisitos
- Java 17+
- MySQL Server
- Scene Builder
- JasperReports Library

## Instalación
1. Clona el repositorio:
   ```bash
   git clone https://github.com/JuanCarlos92/JDBC_Restaurante_crud.git
   ```
2. Importa el proyecto en tu IDE favorito (IntelliJ, Eclipse, NetBeans).
3. Importar la Base de datos y cambiar usuario y contraseña 
4. Ejecuta la aplicación.

---

## Contribuciones
¡Las contribuciones son bienvenidas! Si quieres mejorar el proyecto, por favor crea un **fork**, realiza tus cambios y envía un **pull request**.

## Licencia
Este proyecto está bajo la Licencia MIT.
