# Employee Feedback System

This project is a web-based Employee Feedback System, designed to facilitate efficient collection and analysis of employee opinions within an organization. The application provides a basic administrative panel for managing the system and was developed during an internship at BTC company using Java Spring and Svelte.

## Features

### User Interface
- **Java Spring Backend**: Provides robust API endpoints for data processing and interaction with the database.
- **Svelte Frontend**: A modern and fast user interface built with Svelte, ensuring a smooth user experience.

### System Workflow
1. **API Requests**: The system sends an API request to a designated endpoint containing data formatted in JSON.
2. **Data Processing**: Upon receiving the data, the system processes it, creates an event in the database, and sends an email with a form link to the client.
3. **User Authentication**: The system supports user registration and login functionalities, including password recovery.
    - **Domain Restriction**: Registration is restricted to emails within the `@btc.com.pl` domain, as the application was specifically developed for BTC company.

### Administrative Panel
- **Full Control**: The administrator has full control over the system.
- **Helpdesk Management**: Ability to add and manage helpdesks within the system.
- **Survey Creation**: The administrator can create question sets where responses can be collected in the form of star ratings or simple Yes/No answers.
- **Data Insights**: The administrator can view responses to specific questions, access response statistics, and view rankings of the best-performing employees.

## Installation and Setup

To run this project locally, follow these steps:

1. **Development Environment**:
    - Recommended IDE: IntelliJ IDEA.
    - Required Java Version: Java 17 JDK.

2. **Backend Setup**:
    - Open the project in IntelliJ IDEA.
    - Configure `application.properties` and run a project.

3. **Frontend Setup**:
    - Ensure Node.js and npm are installed on your system.
    - Navigate to the frontend directory.
    - Run the `run-svelte.bat` file to start the Svelte application. This script will handle the installation of dependencies and launch the development server, ensure to set a backend url in `store.js`.

4. **Database Configuration**:
    - The application uses a PostgreSQL database named `emailService`.
    - Ensure that PostgreSQL is installed and running on your machine.
    - Configure the database connection details in the `application.properties` file.

## Usage

- **Administrator**:
    - Log in using your admin credentials.
    - Navigate to the admin panel to manage helpdesks, create surveys, and view employee feedback and statistics.

- **Employee**:
    - Register with your `@btc.com.pl` email.
    - Log in to provide feedback using the links sent to your email.