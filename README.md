# إدارة المهام اليومية — Daily Task Manager (JavaFX)

تطبيق سطح مكتب لإدارة المهام اليومية وتوزيعها وتتبع الوقت، مبني بـ Java 17 و JavaFX مع قاعدة بيانات SQLite محلية.

## المتطلبات

- JDK 17 أو أحدث
- Maven 3.8+
- اتصال إنترنت عند أول بناء (لتحميل الاعتمادات من Maven Central)

## التشغيل

```bash
mvn clean javafx:run
```

قاعدة البيانات تُنشأ تلقائيًا عند أول تشغيل في:
`~/.taskmanager/taskmanager.db`

## بنية المشروع

```
src/main/java/com/taskmanager/
  MainApp.java              نقطة انطلاق التطبيق
  model/                    Task, Priority, Category, TaskStatus, TimeEntry
  database/                 DatabaseManager, TaskDAO, TimeEntryDAO (SQLite + JDBC)
  service/                  TaskService, TimeTrackingService, SchedulingService,
                             GamificationService, StatisticsService
  controller/                MainController, TaskFormController, DashboardController,
                             FocusModeController
  util/                     DateTimeUtils, ExportUtils (CSV/Excel/PDF)
src/main/resources/
  fxml/                      main.fxml, task_form.fxml, dashboard.fxml, focus_mode.fxml
  css/                       light.css, dark.css
```

## الميزات المُنفَّذة

- إضافة/تعديل/حذف المهام (عنوان، وصف، أولوية، تصنيف، موعد نهائي، مدة مقدّرة)
- جدولة تلقائية للمهام على اليوم حسب الأولوية وساعات الذروة، مع دعم اختياري لفواصل Pomodoro
  (`SchedulingService.SchedulingOptions.usePomodoro`)
- اقتراح ذكي لأفضل وقت فراغ متاح لمهمة جديدة
- سحب وإفلات لإعادة ترتيب المهام في جدول اليوم
- مؤقت لكل مهمة (بدء/إيقاف مؤقت/استئناف) وحساب الوقت الفعلي مقابل المقدّر
- لوحة إحصائيات: نسبة الإنجاز، توزيع الوقت حسب التصنيف، اتجاه الإنتاجية، أكثر ساعات الإنتاجية
- نظام نقاط ومستويات وشارات (Gamification)
- وضع التركيز (Focus Mode) بملء الشاشة يعزل المهمة الحالية والمؤقت فقط
- تحليل الأنماط: اكتشاف أكثر ساعات اليوم إنتاجية بناءً على سجل الجلسات
- قائمة "المهام المؤجلة كثيرًا" (أُجّلت مرتين أو أكثر)
- تصدير التقارير إلى CSV / Excel (.xlsx) / PDF
- وضع داكن/فاتح قابل للتبديل
- معالجة أخطاء قاعدة البيانات في طبقة DAO مع رسائل واضحة للمستخدم

## نقاط قابلة للتوسعة لاحقًا

هذه الميزات تتطلب اعتمادات خارجية أو مفاتيح API لم تُضَف افتراضيًا، وتُركت كنقاط امتداد:

- **مزامنة Google Calendar**: يحتاج OAuth2 عبر Google API Client Library وبيانات اعتماد المطوّر.
  نقطة البداية المقترحة: خدمة جديدة `service/GoogleCalendarSyncService.java` تستخدم
  `TaskService.getAllTasks()` كمصدر بيانات.
- **ملاحظات صوتية (Speech-to-Text)**: يحتاج مكتبة تعرّف كلام (مثل Vosk للعمل بدون إنترنت،
  أو Google Speech-to-Text API). نقطة البداية: زر إضافي في `task_form.fxml` يستدعي خدمة
  `VoiceInputService` تُعيد نصًا يُملأ في `titleField`.
- **مراجعة نهاية اليوم التلقائية**: يمكن تفعيلها بإضافة `Timeline`/`ScheduledExecutorService` في
  `MainApp` يفتح نافذة ملخص في وقت يحدده المستخدم من الإعدادات (جدول `user_settings` جاهز لتخزينه).

## ملاحظة حول قاعدة البيانات

تُستخدم SQLite دون خادم منفصل؛ ملف القاعدة يُنشأ ويُدار بالكامل محليًا، وجميع الاستعلامات تمر عبر
`PreparedStatement` لمنع حقن SQL، وتُغلَّف كل استثناءات `SQLException` برسائل عربية واضحة في `TaskDAO`.

jpackage --version

.\build-exe.ps1
# TaskManager-JAVA
