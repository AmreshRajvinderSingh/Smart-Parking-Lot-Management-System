# Smart Parking Lot Management System

A comprehensive JavaFX application for managing parking facilities with real-time statistics, user management, and membership features.

## Overview

The Smart Parking Lot Management System is a JavaFX-based application that provides an efficient solution for managing parking facilities. The system offers real-time tracking of parking spaces, automated fee calculation, user management, and comprehensive statistics for decision-making.



## Features

### Core Functionality
- **Parking Management**: Track and manage parking slots of different sizes (small, medium, large)
- **Vehicle Handling**: Support for different vehicle types (Car, Bike, Truck) with appropriate slot allocation
- **Ticket System**: Automated ticket generation and processing for entry and exit
- **Fee Calculation**: Dynamic fee calculation based on parking duration and vehicle type

### Administrative Features
- **Real-time Statistics**: 
  - Occupancy charts (available vs. occupied slots)
  - Vehicle type distribution visualization
  - Key metrics (total slots, occupancy rate, revenue)
- **User Management**:
  - Complete user administration (create, edit, deactivate accounts)
  - Role-based access control (Admin, Operator, Member)
  - Revenue tracking per user

### Enhanced Capabilities
- **Membership System**: Tiered membership program (Bronze, Silver, Gold) with different discount rates
- **Role-based Access Control**: Different interfaces and permissions based on user role
- **Vehicle Search**: Quick look-up of parked vehicles by license plate
- **Data Persistence**: File-based storage for all system data

## Technical Architecture

### MVC Implementation
- **Model**: Core entities like User, Vehicle, ParkingSlot, ParkingTicket
- **View**: FXML-based UI components with CSS styling
- **Controller**: JavaFX controllers handling user interaction and business logic
- **Service Layer**: Manager classes implementing core business functionality

### Design Patterns
- **Singleton Pattern**: Used for manager classes to ensure single instance
- **Inheritance & Polymorphism**: Vehicle types and their specific behaviors
- **Observer Pattern**: Dynamic UI updates through JavaFX observable collections



## Installation

### Prerequisites
- Java JDK 17 or higher
- JavaFX SDK 17 or higher
- Any IDE that supports Java/JavaFX (Eclipse, IntelliJ IDEA, NetBeans)

### Setup Instructions
1. Clone this repository

2. Open the project in your IDE

3. Set up the JavaFX dependencies:
- Add the JavaFX SDK to your project libraries
- Configure VM options to include JavaFX modules:
  ```
  --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
  ```

4. Run the application:
- Execute `edu.northeastern.csye6200.ParkingApp` as the main class

### Default Login
- Username: `admin`
- Password: `password`


## Implementation Details

### Statistics Tab
The Statistics tab provides real-time insights into parking operations through:
- Dynamic pie charts for occupancy and vehicle type distribution
- Key performance metrics with automatic updates
- Background thread for 30-second refresh cycles

### User Management Tab
The User Management tab offers:
- Complete user listing with role and status information
- Revenue calculation for each user based on parking history
- Edit functionality through modal dialog
- Role-based access control for administrative functions

### Data Persistence
- File-based storage using text files (.dat and .txt)
- Data structures optimized for performance:
  - Maps for O(1) lookups (users, slots, tickets)
  - Queues for FIFO slot assignment
  - Sets for ensuring uniqueness of license plates

## Contributors
- [Your Name](https://github.com/yourusername)
- [Team Member 1](https://github.com/teammember1)
- [Team Member 2](https://github.com/teammember2)
- [Team Member 3](https://github.com/teammember3)

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgements
- Northeastern University - CSYE6200 Object-Oriented Design
- JavaFX community for documentation and resources
- [Any other acknowledgements]
