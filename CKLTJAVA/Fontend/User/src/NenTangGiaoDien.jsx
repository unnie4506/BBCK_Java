import { useEffect, useMemo, useState } from "react";
import {
  HashRouter,
  Link,
  Navigate,
  Outlet,
  Route,
  Routes,
  useLocation,
  useNavigate,
  useParams,
  useSearchParams,
} from "react-router-dom";
import { congKhaiApi } from "./services/congKhaiApi.js";
import { xacThucApi } from "./services/xacThucApi.js";
import { donDatPhongApi } from "./services/donDatPhongApi.js";
import { hoSoApi } from "./services/hoSoApi.js";
import { lichSuApi } from "./services/lichSuApi.js";
import { XacThucProvider, useXacThuc } from "./contexts/XacThucContext.jsx";
import { GioPhongProvider, useGioPhong } from "./contexts/GioPhongContext.jsx";
const goiApi = (loi) =>
  loi?.response?.data?.thongBao || "Không kết nối được máy chủ, thử lại.";
const layDuLieu = (phanHoi) => phanHoi?.data?.duLieu;
const layDanhSach = (duLieu) =>
  Array.isArray(duLieu) ? duLieu : duLieu?.danhSach || duLieu?.items || [];
const tienVnd = (soTien) =>
  `${new Intl.NumberFormat("vi-VN").format(Number(soTien || 0))} ₫`;
const ngayHienThi = (ngay) =>
  ngay
    ? new Intl.DateTimeFormat("vi-VN").format(new Date(`${ngay}T00:00:00`))
    : "—";
const homNay = () => {
  const ngay = new Date();
  return `${ngay.getFullYear()}-${String(ngay.getMonth() + 1).padStart(2, "0")}-${String(ngay.getDate()).padStart(2, "0")}`;
};
export {
  useEffect,
  useMemo,
  useState,
  HashRouter,
  Link,
  Navigate,
  Outlet,
  Route,
  Routes,
  useLocation,
  useNavigate,
  useParams,
  useSearchParams,
  congKhaiApi,
  xacThucApi,
  donDatPhongApi,
  hoSoApi,
  lichSuApi,
  XacThucProvider,
  useXacThuc,
  GioPhongProvider,
  useGioPhong,
  goiApi,
  layDuLieu,
  layDanhSach,
  tienVnd,
  ngayHienThi,
  homNay,
};
