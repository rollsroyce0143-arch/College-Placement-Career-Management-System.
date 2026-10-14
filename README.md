# 🎓 College Placement and Career Management System

A desktop-based application developed using **Java Swing and MySQL** to simplify and organize college placement activities. The system helps manage student information, company details, job openings, eligibility checking, job matching, application tracking, and placement analytics through a centralized dashboard.

## 📌 Project Overview

Managing campus placements manually can be time-consuming, especially when many students and companies are involved. This application provides a centralized system to manage student records and placement-related activities efficiently.

The application uses Java Swing for the graphical user interface and MySQL for database storage.

## ✨ Key Features

- **Dashboard:** View student, company, job, application, and selection statistics.
- **Student Management:** Register students and manage their academic and contact information.
- **Company Management:** Maintain company details and recruitment information.
- **Job Management:** Add job openings and define eligibility requirements.
- **Company-wise Eligibility:** View students who meet a company's eligibility criteria.
- **Job Matching:** Match student profiles against available job requirements.
- **Application Tracking:** Manage and monitor placement application statuses.
- **Placement Analytics:** Review placement-related statistics and application outcomes.
- **MySQL Integration:** Store and retrieve application data from a relational database.
- **Dark UI Theme:** Use a modern desktop interface with a dashboard and navigation menu.

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java | Application logic |
| Java Swing | Desktop graphical user interface |
| JDBC | Database connectivity |
| MySQL | Relational database |
| Maven | Dependency management and build automation |
| MySQL Connector/J | Java-to-MySQL connectivity |
| Git and GitHub | Version control and project hosting |

## 🏗️ System Architecture

The application follows a simple three-layer structure:

1. **Presentation Layer:** Java Swing screens for dashboard and management modules.
2. **Application Layer:** Java classes that implement placement management, eligibility checks, job matching, and application tracking.
3. **Database Layer:** MySQL tables store student, company, job, and application information.

## 📂 Main Modules

### 1. Dashboard
Displays overall placement statistics and provides navigation to the application's modules.

### 2. Student Management
Supports student registration and management of details such as roll number, name, department, email, CGPA, skills, and backlogs.

### 3. Company and Job Management
Manages recruiting companies and their job openings, including job titles and eligibility requirements.

### 4. Company-wise Eligibility
Helps identify students who satisfy the eligibility requirements of a selected company.

### 5. Job Matching
Helps compare student qualifications with available job requirements.

### 6. Application Tracking
Maintains application records and tracks recruitment statuses.

### 7. Placement Analytics
Displays placement-related statistics to support monitoring and analysis.

## ⚙️ Prerequisites

Install the following software before running the application:

- Java JDK 17 or a compatible version
- MySQL Server
- MySQL Workbench (recommended)
- Apache Maven (required to rebuild the project)

## 🚀 How to Run

### Option 1: Run the packaged application

1. Ensure MySQL Server is running.
2. Configure the database connection for your local environment.
3. Open a terminal in the directory containing the runnable JAR.
4. Execute:

```bash
java -jar CollegePlacementSystem.jar
```

### Option 2: Build from source

Clone the repository:

```bash
git clone https://github.com/YOUR-USERNAME/College-Placement-Career-Management-System.git
```

Open the project directory:

```bash
cd College-Placement-Career-Management-System
```

Build the project using Maven:

```bash
mvn clean package
```

Run the packaged application:

```bash
java -jar target/CollegePlacementSystem.jar
```

Replace `YOUR-USERNAME` with your GitHub username and adjust the directory name if necessary.

## 🗄️ Database Configuration

The application uses MySQL for persistent storage.

1. Install and start MySQL Server.
2. Create the database named `college_placement`.
3. Create the tables required by the application.
4. Configure the database URL, username, and password using your local configuration or environment variables.
5. Ensure the MySQL JDBC driver is available through the Maven build.

**Security:** Database credentials are not included in this public repository. Do not commit passwords, private student information, or database exports containing sensitive records.

## 🔐 Security and Data Handling

- Keep database credentials outside the source code.
- Use fictional student records for demonstrations.
- Avoid committing private database backups.
- Restrict database access to authorized users.

## 🎯 Project Objectives

- Centralize college placement information.
- Simplify student and company record management.
- Reduce manual effort in eligibility checking.
- Improve visibility into job applications and recruitment progress.
- Provide placement statistics through a unified dashboard.

## 🔮 Future Enhancements

- Role-based login for administrators, placement officers, and students.
- Automated email notifications for placement updates.
- Resume upload and management.
- PDF and Excel report generation.
- Advanced job recommendation and matching.
- Web-based access for students and placement officers.
- Improved authentication, authorization, and audit logging.

## 👨‍💻 Author

**Karthick M**

Electronics and Communication Engineering Student | Software Development Enthusiast

## 📄 License

Choose and add an appropriate open-source license if you intend to permit others to reuse or modify this project. Without a license, normal copyright restrictions generally remain in effect.

---

⭐ If you find this project useful, consider starring the repository on GitHub.
