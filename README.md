# 🔄 SkillSync

<div align="center">
  <img src="app/src/main/ic_launcher-playstore.png" alt="SkillSync Logo" width="120"/>
  
  [![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
  [![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white)](https://developer.android.com/jetpack/compose)
</div>

## 📱 Descripción

SkillSync es una innovadora aplicación móvil que conecta mentores y aprendices para intercambiar conocimientos y habilidades. La plataforma facilita el aprendizaje colaborativo y el crecimiento mutuo a través de un sistema de intercambio de habilidades.

## ✨ Características Principales

### 🎯 Para Usuarios
- 👤 Perfiles personalizados con roles (Mentor/Aprendiz/Ambos)
- 🔍 Exploración de habilidades por categorías
- 📚 Sistema de niveles de experiencia
- 💬 Chat integrado para comunicación directa
- 📅 Gestión de sesiones de aprendizaje

### 🛠️ Características Técnicas
- 🏗️ Arquitectura MVVM
- 🔥 Firebase (Auth, Firestore, Storage)
- 🎨 UI moderna con Jetpack Compose
- 💾 Persistencia local con Room
- 🌐 Gestión de imágenes con Coil
- 🔒 Autenticación segura

## 🎨 Diseño de UI/UX

- Material Design 3
- Temas dinámicos
- Navegación intuitiva
- Animaciones fluidas
- Diseño responsive

## 📱 Pantallas Principales

1. 🏠 Home
   - Vista general de habilidades populares
   - Acceso rápido a funciones principales

2. 🔍 Explorar
   - Búsqueda de habilidades
   - Filtros por categoría y nivel
   - Tarjetas de enseñanza

3. 💬 Chats
   - Lista de conversaciones
   - Mensajería en tiempo real

4. 👤 Perfil
   - Información personal
   - Gestión de habilidades
   - Configuración de disponibilidad

## 🛠️ Tecnologías Utilizadas

- **Frontend:**
  - Jetpack Compose
  - Material 3
  - Navigation Component
  - ViewModel
  - Coroutines
  - Flow

- **Backend:**
  - Firebase Authentication
  - Cloud Firestore
  - Firebase Storage
  - Firebase Cloud Messaging

- **Almacenamiento Local:**
  - Room Database
  - DataStore Preferences

## 📦 Dependencias Principales

```gradle
dependencies {
    // Jetpack Compose
    implementation "androidx.compose.ui:ui:$compose_version"
    implementation "androidx.compose.material3:material3:$material3_version"
    
    // Firebase
    implementation platform('com.google.firebase:firebase-bom:$firebase_version')
    implementation 'com.google.firebase:firebase-auth-ktx'
    implementation 'com.google.firebase:firebase-firestore-ktx'
    implementation 'com.google.firebase:firebase-storage-ktx'
    
    // Room
    implementation "androidx.room:room-runtime:$room_version"
    implementation "androidx.room:room-ktx:$room_version"
    
    // Otros
    implementation "io.coil-kt:coil-compose:$coil_version"
    implementation "androidx.navigation:navigation-compose:$nav_version"
}
```

## 🚀 Instalación

1. Clona el repositorio
```bash
git clone https://github.com/AngelIsaiSzr/SkillSync.git
```

2. Abre el proyecto en Android Studio

3. Configura tu archivo `google-services.json` de Firebase

4. Ejecuta la aplicación

## 📞 Contacto

👨‍💻 **Angel Salazar**
- 📧 Email: [angelmoreno152000@gmail.com](mailto:angelmoreno152000@gmail.com)
- 🌐 LinkedIn: [Angel Salazar](https://www.linkedin.com/in/angelisaiszr/)
- 💼 GitHub: [@AngelIsaiSzr](https://github.com/AngelIsaiSzr)

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

---

<div align="center">
  Desarrollado con ❤️ por Angel Salazar
</div> 