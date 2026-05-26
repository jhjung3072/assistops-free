export type UserRole = "USER" | "ADMIN";

export type User = {
  id: string;
  email: string;
  name: string;
  role: UserRole;
};

export type AuthResponse = {
  accessToken: string;
  tokenType: "Bearer";
  user: User;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegisterRequest = {
  name: string;
  email: string;
  password: string;
};
