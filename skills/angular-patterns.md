# Angular Patterns

## Архитектура приложения
- Angular 17, standalone components (новые) + NgModules (legacy)
- Angular Material MDC для UI компонентов
- Functional interceptors и guards (Angular 17 style)
- Lazy loading через loadComponent() / loadChildren()

## Структура проекта
```
ui-ng/src/app/
├── core/
│   ├── components/       — глобальные компоненты (event-selector)
│   ├── guards/           — auth.guard.ts
│   ├── interceptors/     — auth.interceptor.ts
│   ├── layout/           — header, sidebar, layouts
│   ├── models/           — auth.model.ts, event.model.ts
│   └── services/         — event-core, websocket, loading, notification, error-handling
├── features/
│   ├── auth/             — login, register, reset-password, invite-success
│   ├── dashboard/        — events, locations, badges, notifications, quotas
│   ├── public-registration/ — публичная регистрация гостей
│   └── scanner/          — QR-сканер
├── shared/
│   └── components/       — guest-table, payment-widget, editors, renderers
└── environments/         — environment.ts
```

## Standalone Components (новый код)
```typescript
@Component({
  selector: 'app-feature',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    RouterModule
  ],
  templateUrl: './feature.component.html',
  styleUrls: ['./feature.component.scss']
})
export class FeatureComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
```

## Routing — Lazy Loading
```typescript
// app.routes.ts:
export const routes: Routes = [
  {
    path: 'auth',
    loadComponent: () => import('./core/layout/auth-layout/auth-layout.component')
      .then(m => m.AuthLayoutComponent),
    children: [
      { path: 'login', loadComponent: () => import('./features/auth/login/login.component')
          .then(m => m.LoginComponent) },
      { path: 'register', loadComponent: () => import('./features/auth/register/register.component')
          .then(m => m.RegisterComponent) },
      { path: 'invite-accepted', loadComponent: () =>
          import('./features/auth/invite-success/invite-success.component')
          .then(m => m.InviteSuccessComponent) }
    ]
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadChildren: () => import('./features/dashboard/dashboard.routes')
      .then(m => m.DASHBOARD_ROUTES)
  }
];
```

## Functional Interceptor (Angular 17)
```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('jwt_token');

  if (token) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Сессия истекла — redirect to login
        localStorage.removeItem('jwt_token');
        inject(Router).navigate(['/auth/login']);
      }
      return throwError(() => error);
    })
  );
};

// Регистрация в app.config.ts:
provideHttpClient(withInterceptors([authInterceptor]))
```

## Functional Guard
```typescript
export const authGuard: CanActivateFn = (route, state) => {
  const token = localStorage.getItem('jwt_token');
  if (token) return true;

  inject(Router).navigate(['/auth/login']);
  return false;
};
```

## Services — State Management
```typescript
@Injectable({ providedIn: 'root' })
export class EventCoreService {
  // BehaviorSubject — хранит текущее значение (можно читать .value):
  private selectedEventSubject = new BehaviorSubject<Event | null>(null);
  selectedEvent$ = this.selectedEventSubject.asObservable();

  // Subject — чистый event stream (нет текущего значения):
  private eventUpdatedSubject = new Subject<Event>();
  eventUpdated$ = this.eventUpdatedSubject.asObservable();

  setSelectedEvent(event: Event | null) {
    this.selectedEventSubject.next(event);
  }

  getSelectedEvent(): Event | null {
    return this.selectedEventSubject.value;  // синхронный доступ
  }
}
```

## RxJS паттерны
```typescript
// takeUntil для cleanup подписок:
this.service.getData()
  .pipe(takeUntil(this.destroy$))
  .subscribe({ next: ..., error: ... });

// finalize для loading state:
this.service.create(data)
  .pipe(finalize(() => this.isLoading = false))
  .subscribe({ next: ..., error: ... });

// map для трансформации:
this.http.get<EventDTO[]>(url).pipe(
  map(dtos => dtos.map(dto => this.convertToEvent(dto)))
);

// switchMap для зависимых запросов:
this.route.params.pipe(
  switchMap(params => this.service.getById(params['id']))
);

// tap для side effects:
this.authService.login(creds).pipe(
  tap(response => localStorage.setItem('token', response.jwt))
);
```

## Reactive Forms
```typescript
// Создание формы:
this.form = this.fb.group({
  email: ['', [Validators.required, Validators.email]],
  password: ['', [Validators.required]],
  role: ['EMPLOYEE', [Validators.required]]
});

// Динамическая валидация:
this.form.get('role')?.valueChanges
  .pipe(takeUntil(this.destroy$))
  .subscribe(role => {
    const locationIds = this.form.get('locationIds');
    if (role === 'GUARD') {
      locationIds?.setValidators([Validators.required]);
    } else {
      locationIds?.clearValidators();
      locationIds?.setValue([]);
    }
    locationIds?.updateValueAndValidity();
  });

// Pre-fill + disable:
this.form.patchValue({ email: info.email });
this.form.get('email')?.disable();
```

## Dialog Integration (Angular Material)
```typescript
// Открытие:
const dialogRef = this.dialog.open(CreateDialogComponent, {
  width: '800px',
  disableClose: true,
  data: { eventId: this.eventId }
});

// Обработка результата:
dialogRef.afterClosed().subscribe(result => {
  if (result) {
    this.refreshData();
  }
});

// Внутри диалога — закрытие:
this.dialogRef.close(true);   // с результатом
this.dialogRef.close();       // без результата (отмена)
```

## Notification Service
```typescript
// Использование:
this.notificationService.success('Приглашение отправлено');
this.notificationService.error(err?.error?.message || 'Ошибка');
this.notificationService.info('Письмо отправлено на email');
this.notificationService.warn('Квота исчерпана');

// Реализация через MatSnackBar:
// duration: 5000ms, position: top-right
// panelClass: success-snackbar | error-snackbar | warning-snackbar | info-snackbar
```

## State Machine для компонентов
```typescript
type PageState = 'loading' | 'success' | 'error';

export class FeatureComponent implements OnInit {
  state: PageState = 'loading';

  ngOnInit(): void {
    this.service.getData()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => { this.data = data; this.state = 'success'; },
        error: () => { this.state = 'error'; }
      });
  }
}
```

## Change Detection
```typescript
// Immutable array update для OnPush:
this.items = [...this.items, newItem];          // добавление
this.items = this.items.filter(i => i.id !== id); // удаление
this.items = this.items.map(i =>                 // обновление
  i.id === updated.id ? updated : i
);
```

## OAuth2 — передача invite token
```typescript
initiateGoogleAuth(inviteToken?: string | null): void {
  const baseUrl = `${this.API_URL}/oauth2/authorization/google`;
  if (inviteToken) {
    window.location.href = `${baseUrl}?invite_token=${encodeURIComponent(inviteToken)}`;
  } else {
    window.location.href = baseUrl;
  }
}
```

## Жёсткие правила
- ЗАПРЕЩЕНО подписываться без cleanup (всегда takeUntil(destroy$) или async pipe)
- ЗАПРЕЩЕНО использовать any — определять интерфейсы для всех моделей
- ЗАПРЕЩЕНО делать HTTP-запросы в компонентах напрямую — только через сервисы
- ЗАПРЕЩЕНО хардкодить API URL — использовать environment.apiUrl
- Новые компоненты — ТОЛЬКО standalone (не NgModule)
- Формы — ТОЛЬКО reactive (не template-driven)
- providedIn: 'root' для singleton-сервисов
- Все подписки в ngOnInit, cleanup в ngOnDestroy
