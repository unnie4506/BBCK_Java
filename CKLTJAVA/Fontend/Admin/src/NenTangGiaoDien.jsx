import { useEffect, useState } from "react";
import {
  HashRouter,
  Link,
  Navigate,
  Route,
  Routes,
  useNavigate,
} from "react-router-dom";
import {
  Alert,
  Button,
  Card,
  DatePicker,
  Form,
  Input,
  InputNumber,
  Layout,
  Menu,
  Modal,
  Select,
  Space,
  Spin,
  Table,
  Tag,
  message,
} from "antd";
import { ConfigProvider } from "antd";
import viVN from "antd/locale/vi_VN";
import dayjs from "dayjs";
import "dayjs/locale/vi";
import { XacThucProvider, useXacThuc } from "./contexts/XacThucContext.jsx";
import { DUONG_DAN_GOC } from "./utils/hangSo.js";
import {
  laySoDo,
  layLoaiPhong,
  themLoaiPhong,
  suaLoaiPhong,
  doiHoatDongLoaiPhong,
  layAnhLoaiPhong,
  taiAnhLoaiPhong,
  xoaAnhLoaiPhong,
  datAnhDaiDienLoaiPhong,
  layTienNghi,
  luuTienNghi,
  xoaTienNghi,
  doiHoatDongTienNghi,
  layPhong,
  layMotPhong,
  luuPhong,
  doiHoatDongPhong,
  doiTrangThaiPhong,
  taiAnhPhong,
  xoaAnhPhong,
  datAnhDaiDien,
  layKhach,
  layMotKhach,
  luuKhach,
  doiHoatDongKhach,
} from "./services/danhMucApi.js";
import { donDatPhongApi } from "./services/donDatPhongApi.js";
import { chinhSachApi } from "./services/chinhSachApi.js";
import { hoanCocApi } from "./services/hoanCocApi.js";
const { Header, Sider, Content } = Layout;
const DUONG_DAN = (import.meta.env.VITE_DUONG_DAN_QUAN_LY || "").replace(
  /^\/+|\/+$/g,
  "",
);
const MENUS = [
  ["so-do-phong", "Sơ đồ phòng"],
  ["don-dat-phong", "Đơn đặt phòng"],
  ["cho-xac-nhan-coc", "Chờ xác nhận cọc"],
  ["khach-hang", "Khách hàng"],
  ["loai-phong", "Loại phòng và giá"],
  ["phong", "Phòng"],
  ["tien-nghi", "Tiện nghi"],
  ["chinh-sach-hoan-huy", "Chính sách hoàn hủy"],
  ["hoan-coc", "Hoàn cọc chờ xử lý"],
];
const layThongBao = (loi) =>
  loi?.response?.data?.thongBao ||
  loi?.response?.data?.message ||
  "Không thể hoàn thành yêu cầu.";
const layDanhSach = (ketQua) =>
  Array.isArray(ketQua) ? ketQua : ketQua?.danhSach || ketQua?.items || [];
const dinhDangTien = (giaTri) =>
  `${new Intl.NumberFormat("vi-VN").format(Number(giaTri || 0))} ₫`;
const dinhDangNgay = (giaTri) =>
  giaTri ? dayjs(giaTri).format("DD/MM/YYYY") : "—";
export {
  useEffect,
  useState,
  HashRouter,
  Link,
  Navigate,
  Route,
  Routes,
  useNavigate,
  Alert,
  Button,
  Card,
  DatePicker,
  Form,
  Input,
  InputNumber,
  Layout,
  Menu,
  Modal,
  Select,
  Space,
  Spin,
  Table,
  Tag,
  message,
  ConfigProvider,
  viVN,
  dayjs,
  XacThucProvider,
  useXacThuc,
  DUONG_DAN_GOC,
  laySoDo,
  layLoaiPhong,
  themLoaiPhong,
  suaLoaiPhong,
  doiHoatDongLoaiPhong,
  layAnhLoaiPhong,
  taiAnhLoaiPhong,
  xoaAnhLoaiPhong,
  datAnhDaiDienLoaiPhong,
  layTienNghi,
  luuTienNghi,
  xoaTienNghi,
  doiHoatDongTienNghi,
  layPhong,
  layMotPhong,
  luuPhong,
  doiHoatDongPhong,
  doiTrangThaiPhong,
  taiAnhPhong,
  xoaAnhPhong,
  datAnhDaiDien,
  layKhach,
  layMotKhach,
  luuKhach,
  doiHoatDongKhach,
  donDatPhongApi,
  chinhSachApi,
  hoanCocApi,
  Header,
  Sider,
  Content,
  DUONG_DAN,
  MENUS,
  layThongBao,
  layDanhSach,
  dinhDangTien,
  dinhDangNgay,
};
