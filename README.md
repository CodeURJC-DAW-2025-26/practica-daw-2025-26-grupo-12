# [Nombre de la Aplicación]

## 👥 Miembros del Equipo
| Nombre y Apellidos | Correo URJC | Usuario GitHub |
|:--- |:--- |:--- |
| [Nombre 1] | [email1]@alumnos.urjc.es | [User1] |
| [Nombre 2] | [email2]@alumnos.urjc.es | [User2] |
| [Nombre 3] | [email3]@alumnos.urjc.es | [User3] |
| [Nombre 4] | [email4]@alumnos.urjc.es | [User4] |

---

## 🎭 **Preparación 1: Definición del Proyecto**

### **Descripción del Tema**
[Escribe aquí una descripción breve y concisa de qué trata tu aplicación, el sector al que pertenece y qué valor aporta al usuario].

### **Entidades**
Indicar las entidades principales que gestionará la aplicación y las relaciones entre ellas:

1. **[Entidad 1]**: [Ej: Usuario]
2. **[Entidad 2]**: [Ej: Producto]
3. **[Entidad 3]**: [Ej: Pedido]
4. **[Entidad 4]**: [Ej: Categoría]

**Relaciones entre entidades:**
- [Ej: Usuario - Pedido: Un usuario puede tener múltiples pedidos (1:N)]
- [Ej: Pedido - Producto: Un pedido puede contener múltiples productos y un producto puede estar en múltiples pedidos (N:M)]
- [Ej: Producto - Categoría: Un producto pertenece a una categoría (N:1)]
- [Descripción de otras relaciones relevantes]

### **Permisos de los Usuarios**
Describir los permisos de cada tipo de usuario e indicar de qué entidades es dueño:

* **Usuario Anónimo**: 
  - Permisos: [Ej: Visualización de catálogo, búsqueda de productos, registro]
  - No es dueño de ninguna entidad

* **Usuario Registrado**: 
  - Permisos: [Ej: Gestión de perfil, realizar pedidos, crear valoraciones]
  - Es dueño de: [Ej: Sus propios Pedidos, su Perfil de Usuario, sus Valoraciones]

* **Administrador**: 
  - Permisos: [Ej: Gestión completa de productos (CRUD), visualización de estadísticas, moderación de contenido]
  - Es dueño de: [Ej: Productos, Categorías, puede gestionar todos los Pedidos y Usuarios]

### **Imágenes**
Indicar qué entidades tendrán asociadas una o varias imágenes:

- **[Entidad con imágenes 1]**: [Ej: Usuario - Una imagen de avatar por usuario]
- **[Entidad con imágenes 2]**: [Ej: Producto - Múltiples imágenes por producto (galería)]
- **[Entidad con imágenes 3]**: [Ej: Categoría - Una imagen representativa por categoría]

### **Gráficos**
Indicar qué información se mostrará usando gráficos y de qué tipo serán:

- **Gráfico 1**: [Ej: Ventas mensuales - Gráfico de barras]
- **Gráfico 2**: [Ej: Productos más vendidos - Gráfico de tarta/circular]
- **Gráfico 3**: [Ej: Evolución de usuarios registrados - Gráfico de líneas]
- **Gráfico 4**: [Ej: Distribución de pedidos por categoría - Gráfico de barras horizontales]

### **Tecnología Complementaria**
Indicar qué tecnología complementaria se empleará:

- [Ej: Envío de correos electrónicos automáticos mediante JavaMailSender]
- [Ej: Generación de PDFs de facturas usando iText o similar]
- [Ej: Sistema de autenticación OAuth2 o JWT]
- [Otras tecnologías externas que se integrarán]

### **Algoritmo o Consulta Avanzada**
Indicar cuál será el algoritmo o consulta avanzada que se implementará:

- **Algoritmo/Consulta**: [Ej: Sistema de recomendaciones basado en el historial de compras del usuario]
- **Descripción**: [Ej: Analiza los productos comprados previamente y sugiere productos similares o complementarios utilizando filtrado colaborativo]
- **Alternativa**: [Ej: Consulta compleja que agrupe ventas por categoría, mes y región, con cálculo de tendencias]

---

## 🛠 **Preparación 2: Maquetación de páginas con HTML y CSS**

### **Vídeo de Demostración**
📹 **[Enlace al vídeo en YouTube](https://www.youtube.com/watch?v=x91MPoITQ3I)**
> Vídeo mostrando las principales funcionalidades de la aplicación web.

### **Diagrama de Navegación**
Diagramas que muestran cómo se navega entre las diferentes páginas de la aplicación:

#### **Navegación Anónima**
![Navegación Anónima](images/anonimo-navegacion.svg)

#### **Navegación Usuario Autenticado**
![Navegación Autenticado](images/autenticado-navegacion.svg)

#### **Navegación Administrador**
![Navegación Admin](images/admin-navegacion.svg)

#### **Relación Entre Paquetes**
![Relación Navegación](images/relacion-navegacion.svg)

> La navegación está separada en tres paquetes (anónimo, autenticado y admin) con sus rutas específicas y puntos de transición.

### **Capturas de Pantalla y Descripción de Páginas**

#### **1. index.html**
![index.html](images/screenshots/index.png)

> Esta es la página principal que se muestra a los usuarios cuando acceden a la aplicación sin haber iniciado sesión. Presenta una vista general de la plataforma y ofrece opciones para iniciar sesión o registrarse, actuando como punto de entrada público a la web.

#### **2. home-auth.html**
![home-auth.html](images/screenshots/home-auth.png)

> Esta es la página Home que se muestra a los usuarios autenticados. Se presentan datos personalizados, como los bots del usuario, estadísticas relevantes y un listado de los últimos torneos en los que ha participado, proporcionando una visión general de su actividad dentro de la plataforma.

#### **3. home-admin.html**
![home-admin.html](images/screenshots/home-admin.png)

> Esta es la página principal del administrador. Su diseño es similar al de la página pública, pero adaptada al rol de administrador, sustituyendo los botones de registro e inicio de sesión por una opción para cerrar sesión. Desde aquí el administrador puede navegar hacia las secciones de gestión.

---

### **Autenticación / Auth**

#### **4. login.html**
![login.html](images/screenshots/login.png)

> Esta es la página de inicio de sesión de los usuarios. Permite acceder a la plataforma introduciendo las credenciales de la cuenta y ofrece opciones adicionales como iniciar sesión con Google (previsto para futuras prácticas), navegar a la página de registro o iniciar sesión como administrador.

#### **5. sign-up.html**
![sign-up.html](images/screenshots/sign-up.png)

> Esta es la página de registro para nuevos usuarios. Contiene un formulario con los campos necesarios para crear una cuenta (correo electrónico, nombre de usuario, contraseña y confirmación de contraseña). También incluye un enlace directo a la página de login para usuarios que ya disponen de una cuenta.

---

### **Bots**

#### **6. bot-create.html**
![bot-create.html](images/screenshots/bot-create.png)

> Esta es la página destinada a la creación de bots por parte del usuario. Se presenta como un formulario donde se pueden definir las características del bot y se incluyen opciones para importar el código del bot desde archivos en Python o JavaScript.

#### **7. bot-detail.html**
![bot-detail.html](images/screenshots/bot-detail.png)

> Esta página muestra los detalles de un bot cuando el usuario no está autenticado. Permite consultar información básica y estadísticas del bot. Existen versiones análogas para usuarios autenticados y administradores, diferenciándose principalmente en las opciones de navegación y botones disponibles.

#### **8. bot-detail-admin.html**
![bot-detail-admin.html](images/screenshots/bot-detail-admin.png)

> Esta es la versión de la página de detalles del bot para el administrador. Ofrece la misma información general del bot, pero con opciones adicionales de navegación y control propias del rol administrativo.

#### **9. bot-edit.html**
![bot-edit.html](images/screenshots/bot-edit.png)

> Esta es la página que permite a un usuario autenticado modificar las propiedades de su bot. Desde aquí puede editar sus parámetros y realizar pruebas mediante test matches para evaluar su rendimiento antes de competir en torneos.

#### **10. my-bots.html**
![my-bots.html](images/screenshots/my-bots.png)

> Esta es la página de “Mis Bots” para usuarios autenticados. Permite consultar todos los bots creados por el usuario, mostrando su nombre, estrategia, ELO y etiquetas de características. Incluye botones para ver o editar cada bot, así como opciones rápidas para crear un nuevo bot, importar bots existentes o filtrar la lista. En un panel lateral se presenta un resumen con estadísticas del usuario, como el total de bots y el ELO más alto, junto con accesos directos a acciones frecuentes, como abrir el bot más reciente o ver partidas recientes.

---

### **Torneos / Tournaments**

#### **11. admin-tournament-create-admin.html**
![admin-tournament-create-admin.html](images/screenshots/admin-tournament-create-admin.png)

> Esta es la página destinada a la creación de torneos por parte del administrador. Se compone de un formulario donde se introducen los datos principales del torneo, como título, número máximo de jugadores, fechas, formato y descripción. También permite añadir información opcional como premio e imagen representativa mediante un modal de subida de archivos.

#### **12. admin-tournament-detail-admin.html**
![admin-tournament-detail-admin.html](images/screenshots/admin-tournament-detail-admin.png)

> Esta página muestra la información detallada de un torneo desde el punto de vista del administrador. Incluye un resumen con el estado del torneo, fechas clave, número de participantes y formato de competición, además de acciones administrativas y tabla con los bots participantes.

#### **13. admin-tournament-edit-admin.html**
![admin-tournament-edit-admin.html](images/screenshots/admin-tournament-edit-admin.png)

> Página utilizada por el administrador para modificar la configuración de un torneo existente. Se pueden cambiar parámetros como fecha de inicio, número máximo de jugadores, estado del torneo y añadir notas internas. Incluye opciones para guardar cambios o resetear campos.

#### **14. tournament-create.html**
![tournament-create.html](images/screenshots/tournament-create.png)

> Página de creación de torneos accesible desde la vista pública. No permite crear torneos directamente, muestra un mensaje indicando que esta funcionalidad es exclusiva del panel de administración e incluye accesos rápidos para iniciar sesión.

#### **15. tournament-detail-auth.html**
![tournament-detail-auth.html](images/screenshots/tournament-detail-auth.png)

> Muestra información completa del torneo, incluyendo fechas, premio, formato, número de plazas y organizador, además de descripción y reglas. La inscripción está cerrada y el botón correspondiente aparece deshabilitado.

#### **16. tournament-detail-open-auth.html**
![tournament-detail-open-auth.html](images/screenshots/tournament-detail-open-auth.png)

> Muestra la información principal del torneo con inscripciones activas. Incluye descripción, reglas y un botón para unirse al torneo, además de otro para regresar a la lista.

#### **17. tournament-detail-open.html**
![tournament-detail-open.html](images/screenshots/tournament-detail-open.png)

> Versión pública de la página de detalles de un torneo abierto, accesible sin autenticación. Permite consultar información pero restringe la acción de inscripción.

#### **18. tournament-detail.html**
![tournament-detail.html](images/screenshots/tournament-detail.png)

> Página pública de detalles de un torneo cuyo periodo de inscripción aún no está abierto o está cerrado. La acción de registro aparece deshabilitada.

#### **19. tournament-join.html**
![tournament-join.html](images/screenshots/tournament-join.png)

> Permite seleccionar un bot del usuario para participar en el torneo, añadir nota opcional y aceptar reglas. Muestra resumen del torneo y recompensas, con botones para confirmar o cancelar la inscripción.

#### **20. tournament-list-auth.html**
![tournament-list-auth.html](images/screenshots/tournament-list-auth.png)

> Presenta los torneos organizados por categorías según su estado: abiertos, en progreso, próximos y finalizados. Incluye enlaces a detalles y acciones según el estado del torneo.

#### **21. tournament-list.html**
![tournament-list.html](images/screenshots/tournament-list.png)

> Versión pública del listado de torneos, accesible sin autenticación, con restricciones en acciones y redirecciones a páginas informativas o de login cuando se requiere.

#### **22. tournament-result-auth.html**
![tournament-result-auth.html](images/screenshots/tournament-result-auth.png)

> Presenta resumen del torneo finalizado, ganador, premio, tabla de resultados, ranking Top 8 y gráficos de uso de movimientos. Incluye opciones de descarga de informes.

#### **23. tournament-results.html**
![tournament-result.html](images/screenshots/tournament-results.png)

> Versión pública de la página de resultados del torneo, con la misma información visual y estadísticas que la versión autenticada.

#### **24. my-tournaments-auth.html**
![my-tournaments-auth.html](images/screenshots/my-tournaments-auth.png)

> Lista los torneos en los que el usuario está registrado o ha participado, con filtros, búsqueda y enlaces a detalles o resultados finales. Muestra resumen visual con badges y botones de acción contextualizados.


### **Partidas / Matches**

#### **25. match-battle.html**
![match-battle.html](images/screenshots/match-battle.png)

> Página que representa el estado de una partida en curso entre dos bots, con nombres, identificadores gráficos, indicador “VS” y spinner de carga. Incluye botón para acceder a resultados.

#### **26. match-list-admin.html**
![match-list-admin.html](images/screenshots/match-list-admin.png)

> Tabla con mejores enfrentamientos, criterios temporales, identificador de partida, bots, ELO máximo, resultado y fecha. Permite visualizar detalles y cuenta con paginación.

#### **27. match-list-auth.html**
![match-list-auth.html](images/screenshots/match-list-auth.png)

> Similar a la versión administrativa, con tabla de información básica y filtros temporales. Permite acceder a estadísticas detalladas de cada partida y paginación.

#### **28. match-list.html**
![match-list.html](images/screenshots/match-list.png)

> Tabla informativa de enfrentamientos destacados accesible sin autenticación, con paginación y filtros temporales.

#### **29. match-search.html**
![match-search.html](images/screenshots/match-search.png)

> Página que indica que el sistema está buscando un oponente adecuado, mostrando indicador visual de carga y opciones para cancelar o forzar inicio de partida.

#### **30. match-stats.html**
![match-stats.html](images/screenshots/match-stats.png)

> Muestra información detallada de un combate entre dos bots, incluyendo marcador final, ELO, cronología de rondas y gráficos. No permite acciones de usuario como rematch.

#### **31. match-stats-auth.html**
![match-stats-auth.html](images/screenshots/match-stats-auth.png)

> Contiene la misma información que la versión pública, con funcionalidades adicionales como botón de “Rematch” y enlaces a perfiles de los bots.

#### **32. match-stats-admin.html**
![match-stats-admin.html](images/screenshots/match-stats-admin.png)

> Incluye toda la información visual y analítica de la partida, adaptada a administrador con controles de navegación y gestión.

#### **33. recent-matches.html**
![recent-matches.html](images/screenshots/recent-matches.png)

> Listado de últimos enfrentamientos del usuario, con ID, bots, resultado, fecha y acceso a estadísticas detalladas. Incluye filtros por tipo de resultado.

---

### **Perfil de Usuario / User Details**

#### **34. user-detail-auth.html**
![user-detail-auth.html](images/screenshots/user-detail-auth.png)

> Muestra datos personales, estadísticas generales, Top Bots y actividad reciente. Permite actualizar foto de perfil y navegar a otras secciones.

#### **35. user-detail-from-bot-auth.html**
![user-detail-from-bot-auth.html](images/screenshots/user-detail-from-bot-auth.png)

> Similar a la página de perfil propio, pero muestra información de otro usuario desde la vista de un bot, con opción de volver al detalle del bot.

#### **36. user-detail-from-bot-admin.html**
![user-detail-from-bot-admin.html](images/screenshots/user-detail-from-bot-admin.png)

> Versión para administrador cuando accede desde la vista de un bot. Incluye navegación adaptada al rol y controles administrativos.

#### **37. user-detail.html**
![user-detail.html](images/screenshots/user-detail.png)

> Versión pública del perfil de usuario, accesible sin autenticación. Muestra información básica, Top Bots y actividad reciente, sin posibilidad de edición ni acciones administrativas.


## 🛠 **Práctica 1: Web con HTML generado en servidor y AJAX**

### **Vídeo de Demostración**
📹 **[Enlace al vídeo en YouTube](https://www.youtube.com/watch?v=x91MPoITQ3I)**
> Vídeo mostrando las principales funcionalidades de la aplicación web.

### **Navegación y Capturas de Pantalla**

#### **Diagrama de Navegación**

Solo si ha cambiado.

#### **Capturas de Pantalla Actualizadas**

Solo si han cambiado.

### **Instrucciones de Ejecución**

#### **Requisitos Previos**
- **Java**: versión 21 o superior
- **Maven**: versión 3.8 o superior
- **MySQL**: versión 8.0 o superior
- **Git**: para clonar el repositorio

#### **Pasos para ejecutar la aplicación**

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/[usuario]/[nombre-repositorio].git
   cd [nombre-repositorio]
   ```

2. **AQUÍ INDICAR LO SIGUIENTES PASOS**

#### **Credenciales de prueba**
- **Usuario Admin**: usuario: `admin`, contraseña: `admin`
- **Usuario Registrado**: usuario: `user`, contraseña: `user`

### **Diagrama de Entidades de Base de Datos**

Diagrama mostrando las entidades, sus campos y relaciones:

![Diagrama Entidad-Relación](images/database-diagram.png)

> [Descripción opcional: Ej: "El diagrama muestra las 4 entidades principales: Usuario, Producto, Pedido y Categoría, con sus respectivos atributos y relaciones 1:N y N:M."]

### **Diagrama de Clases y Templates**

Diagrama de clases de la aplicación con diferenciación por colores o secciones:

![Diagrama de Clases](images/classes-diagram.png)

> [Descripción opcional del diagrama y relaciones principales]

### **Participación de Miembros en la Práctica 1**

#### **Alumno 1 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 2 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 3 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 4 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

## 🛠 **Práctica 2: Incorporación de una API REST a la aplicación web, despliegue con Docker y despliegue remoto**

### **Vídeo de Demostración**
📹 **[Enlace al vídeo en YouTube](https://www.youtube.com/watch?v=x91MPoITQ3I)**
> Vídeo mostrando las principales funcionalidades de la aplicación web.

### **Documentación de la API REST**

#### **Especificación OpenAPI**
📄 **[Especificación OpenAPI (YAML)](/api-docs/api-docs.yaml)**

#### **Documentación HTML**
📖 **[Documentación API REST (HTML)](https://raw.githack.com/[usuario]/[repositorio]/main/api-docs/api-docs.html)**

> La documentación de la API REST se encuentra en la carpeta `/api-docs` del repositorio. Se ha generado automáticamente con SpringDoc a partir de las anotaciones en el código Java.

### **Diagrama de Clases y Templates Actualizado**

Diagrama actualizado incluyendo los @RestController y su relación con los @Service compartidos:

![Diagrama de Clases Actualizado](images/complete-classes-diagram.png)

### **Instrucciones de Ejecución con Docker**

#### **Requisitos previos:**
- Docker instalado (versión 20.10 o superior)
- Docker Compose instalado (versión 2.0 o superior)

#### **Pasos para ejecutar con docker-compose:**

1. **Clonar el repositorio** (si no lo has hecho ya):
   ```bash
   git clone https://github.com/[usuario]/[repositorio].git
   cd [repositorio]
   ```

2. **AQUÍ LOS SIGUIENTES PASOS**:

### **Construcción de la Imagen Docker**

#### **Requisitos:**
- Docker instalado en el sistema

#### **Pasos para construir y publicar la imagen:**

1. **Navegar al directorio de Docker**:
   ```bash
   cd docker
   ```

2. **AQUÍ LOS SIGUIENTES PASOS**

### **Despliegue en Máquina Virtual**

#### **Requisitos:**
- Acceso a la máquina virtual (SSH)
- Clave privada para autenticación
- Conexión a la red correspondiente o VPN configurada

#### **Pasos para desplegar:**

1. **Conectar a la máquina virtual**:
   ```bash
   ssh -i [ruta/a/clave.key] [usuario]@[IP-o-dominio-VM]
   ```
   
   Ejemplo:
   ```bash
   ssh -i ssh-keys/app.key vmuser@10.100.139.XXX
   ```

2. **AQUÍ LOS SIGUIENTES PASOS**:

### **URL de la Aplicación Desplegada**

🌐 **URL de acceso**: `https://[nombre-app].etsii.urjc.es:8443`

#### **Credenciales de Usuarios de Ejemplo**

| Rol | Usuario | Contraseña |
|:---|:---|:---|
| Administrador | admin | admin123 |
| Usuario Registrado | user1 | user123 |
| Usuario Registrado | user2 | user123 |

### **Participación de Miembros en la Práctica 2**

#### **Alumno 1 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 2 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 3 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 4 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

## 🛠 **Práctica 3: Implementación de la web con arquitectura SPA**

### **Vídeo de Demostración**
📹 **[Enlace al vídeo en YouTube](URL_del_video)**
> Vídeo mostrando las principales funcionalidades de la aplicación web.

### **Preparación del Entorno de Desarrollo**

#### **Requisitos Previos**
- **Node.js**: versión 18.x o superior
- **npm**: versión 9.x o superior (se instala con Node.js)
- **Git**: para clonar el repositorio

#### **Pasos para configurar el entorno de desarrollo**

1. **Instalar Node.js y npm**
   
   Descarga e instala Node.js desde [https://nodejs.org/](https://nodejs.org/)
   
   Verifica la instalación:
   ```bash
   node --version
   npm --version
   ```

2. **Clonar el repositorio** (si no lo has hecho ya)
   ```bash
   git clone https://github.com/[usuario]/[nombre-repositorio].git
   cd [nombre-repositorio]
   ```

3. **Navegar a la carpeta del proyecto React**
   ```bash
   cd frontend
   ```

4. **AQUÍ LOS SIGUIENTES PASOS**

### **Diagrama de Clases y Templates de la SPA**

Diagrama mostrando los componentes React, hooks personalizados, servicios y sus relaciones:

![Diagrama de Componentes React](images/spa-classes-diagram.png)

### **Participación de Miembros en la Práctica 3**

#### **Alumno 1 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 2 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 3 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 4 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |
