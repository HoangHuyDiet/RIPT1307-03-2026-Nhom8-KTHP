import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import dayjs from 'dayjs';

export interface ExportTransaction {
  id?: number;
  date: string;
  description: string;
  category?: string;
  categoryName?: string;
  fund?: string;
  personalFundName?: string;
  type: string; // 'INCOME' | 'EXPENSE'
  amount: number;
}

interface ExportSummary {
  totalIncome: number;
  totalExpense: number;
  balance: number;
}

const formatCurrencyPlain = (value: number): string => {
  const absValue = Math.abs(value);
  return new Intl.NumberFormat('vi-VN').format(absValue);
};

const formatDateForDisplay = (dateStr: string): string => {
  const date = dayjs(dateStr);
  return date.isValid() ? date.format('DD/MM/YYYY HH:mm') : dateStr;
};

const sanitizeFileName = (name: string): string => {
  return name
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd')
    .replace(/Đ/g, 'D')
    .replace(/[^a-zA-Z0-9\s-]/g, '')
    .replace(/\s+/g, '-')
    .toLowerCase();
};

const calculateSummary = (transactions: ExportTransaction[]): ExportSummary => {
  let totalIncome = 0;
  let totalExpense = 0;

  transactions.forEach((tx) => {
    const amount = Math.abs(Number(tx.amount) || 0);
    if (tx.type === 'INCOME') {
      totalIncome += amount;
    } else {
      totalExpense += amount;
    }
  });

  return {
    totalIncome,
    totalExpense,
    balance: totalIncome - totalExpense,
  };
};

const getCategoryDisplay = (tx: ExportTransaction): string => {
  return tx.category || tx.categoryName || '';
};

const getFundDisplay = (tx: ExportTransaction): string => {
  return tx.fund || tx.personalFundName || '';
};

export function exportTransactionsToExcel(
  transactions: ExportTransaction[],
  fundName?: string,
  fileName?: string,
): void {
  if (!transactions || transactions.length === 0) {
    console.warn('Không có giao dịch nào để xuất.');
    return;
  }

  const summary = calculateSummary(transactions);
  const today = dayjs().format('DD/MM/YYYY');
  const safeFundName = fundName || 'Tat-ca-quy';
  const outputFileName =
    fileName ||
    `sao-ke-${sanitizeFileName(safeFundName)}-${dayjs().format('DD-MM-YYYY')}`;

  const wsData: (string | number)[][] = [];

  wsData.push(['SMART FINANCE HUB']);
  wsData.push([`Báo cáo sao kê giao dịch - ${fundName || 'Tất cả quỹ'}`]);
  wsData.push([`Ngày xuất: ${today}`]);
  wsData.push([]);

  wsData.push(['STT', 'Ngày', 'Mô tả', 'Danh mục', 'Quỹ', 'Loại', 'Số tiền (đ)']);

  transactions.forEach((tx, index) => {
    const amount = Number(tx.amount) || 0;
    const isIncome = tx.type === 'INCOME';
    wsData.push([
      index + 1,
      formatDateForDisplay(tx.date),
      tx.description || '',
      getCategoryDisplay(tx),
      getFundDisplay(tx),
      isIncome ? 'THU' : 'CHI',
      isIncome ? amount : -Math.abs(amount),
    ]);
  });

  wsData.push([]); // Empty row
  wsData.push(['', '', '', '', '', 'Tổng thu:', summary.totalIncome]);
  wsData.push(['', '', '', '', '', 'Tổng chi:', -summary.totalExpense]);
  wsData.push(['', '', '', '', '', 'Số dư:', summary.balance]);

  const ws = XLSX.utils.aoa_to_sheet(wsData);

  ws['!cols'] = [
    { wch: 5 },   // STT
    { wch: 20 },  // Ngày
    { wch: 35 },  // Mô tả
    { wch: 15 },  // Danh mục
    { wch: 18 },  // Quỹ
    { wch: 10 },  // Loại
    { wch: 18 },  // Số tiền
  ];

  ws['!merges'] = [
    { s: { r: 0, c: 0 }, e: { r: 0, c: 6 } }, // Title row
    { s: { r: 1, c: 0 }, e: { r: 1, c: 6 } }, // Subtitle row
    { s: { r: 2, c: 0 }, e: { r: 2, c: 6 } }, // Date row
  ];

  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'Sao kê giao dịch');

  const excelBuffer = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });
  const blob = new Blob([excelBuffer], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  });
  saveAs(blob, `${outputFileName}.xlsx`);
}

export async function exportTransactionsToPDF(
  transactions: ExportTransaction[],
  fundName?: string,
  fileName?: string,
): Promise<void> {
  if (!transactions || transactions.length === 0) {
    console.warn('Không có giao dịch nào để xuất.');
    return;
  }

  const summary = calculateSummary(transactions);
  const today = dayjs().format('DD/MM/YYYY HH:mm');
  const safeFundName = fundName || 'Tat-ca-quy';
  const outputFileName =
    fileName ||
    `sao-ke-${sanitizeFileName(safeFundName)}-${dayjs().format('DD-MM-YYYY')}`;

  const doc = new jsPDF({
    orientation: 'portrait',
    unit: 'mm',
    format: 'a4',
  });

  try {
    const fontUrl = 'https://cdnjs.cloudflare.com/ajax/libs/ink/3.1.10/fonts/Roboto/roboto-regular-webfont.ttf';
    const res = await fetch(fontUrl);
    const buffer = await res.arrayBuffer();
    const bytes = new Uint8Array(buffer);
    let binary = '';
    for (let i = 0; i < bytes.byteLength; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    const base64Font = btoa(binary);
    doc.addFileToVFS('Roboto-Regular.ttf', base64Font);
    doc.addFont('Roboto-Regular.ttf', 'Roboto', 'normal');
    doc.addFont('Roboto-Regular.ttf', 'Roboto', 'bold');
    doc.setFont('Roboto');
  } catch (error) {
    console.warn('Lỗi tải font tiếng Việt:', error);
  }

  const pageWidth = doc.internal.pageSize.getWidth();
  const margin = 14;

  doc.setFillColor(26, 115, 232); // #1A73E8
  doc.rect(0, 0, pageWidth, 28, 'F');

  doc.setTextColor(255, 255, 255);
  doc.setFontSize(18);
  doc.setFont('Roboto', 'bold');
  doc.text('SMART FINANCE HUB', margin, 12);

  doc.setFontSize(10);
  doc.setFont('Roboto', 'normal');
  doc.text('Báo cáo sao kê giao dịch', margin, 19);

  doc.setFontSize(9);
  doc.text(`Ngay xuat: ${today}`, pageWidth - margin, 12, { align: 'right' });

  let yPos = 36;

  doc.setTextColor(32, 33, 36); // #202124
  doc.setFontSize(12);
  doc.setFont('Roboto', 'bold');
  doc.text(`Quỹ: ${fundName || 'Tất cả quỹ'}`, margin, yPos);

  yPos += 6;
  doc.setFontSize(9);
  doc.setFont('Roboto', 'normal');
  doc.setTextColor(95, 99, 104); // #5F6368
  doc.text(`Số lượng giao dịch: ${transactions.length}`, margin, yPos);

  const sortedDates = transactions
    .map((tx) => dayjs(tx.date))
    .filter((d) => d.isValid())
    .sort((a, b) => a.valueOf() - b.valueOf());

  if (sortedDates.length > 0) {
    const fromDate = sortedDates[0].format('DD/MM/YYYY');
    const toDate = sortedDates[sortedDates.length - 1].format('DD/MM/YYYY');
    doc.text(`Thời gian: ${fromDate} - ${toDate}`, margin + 60, yPos);
  }

  yPos += 8;

  const tableHead = [['STT', 'Ngày', 'Mô tả', 'Danh mục', 'Loại', 'Số tiền (đ)']];

  const tableBody = transactions.map((tx, index) => {
    const amount = Number(tx.amount) || 0;
    const isIncome = tx.type === 'INCOME';
    const amountStr = isIncome
      ? `+${formatCurrencyPlain(amount)}`
      : `-${formatCurrencyPlain(amount)}`;

    return [
      String(index + 1),
      formatDateForDisplay(tx.date),
      tx.description || '',
      getCategoryDisplay(tx),
      isIncome ? 'THU' : 'CHI',
      amountStr,
    ];
  });

  autoTable(doc, {
    startY: yPos,
    head: tableHead,
    body: tableBody,
    theme: 'grid',
    headStyles: {
      font: 'Roboto',
      fillColor: [26, 115, 232],
      textColor: [255, 255, 255],
      fontStyle: 'bold',
      fontSize: 9,
      halign: 'center',
    },
    bodyStyles: {
      font: 'Roboto',
      fontSize: 8,
      textColor: [32, 33, 36],
      cellPadding: 3,
    },
    columnStyles: {
      0: { halign: 'center', cellWidth: 12 },  // STT
      1: { cellWidth: 32 },                      // Ngày
      2: { cellWidth: 'auto' },                   // Mô tả
      3: { cellWidth: 28 },                       // Danh mục
      4: { halign: 'center', cellWidth: 16 },     // Loại
      5: { halign: 'right', cellWidth: 30 },      // Số tiền
    },
    alternateRowStyles: {
      fillColor: [241, 244, 247], // #F1F4F7
    },
    styles: {
      overflow: 'linebreak',
      lineColor: [224, 224, 224],
      lineWidth: 0.3,
    },
    margin: { left: margin, right: margin },
    didParseCell: (data) => {

      if (data.section === 'body' && data.column.index === 5) {
        const text = String(data.cell.raw || '');
        if (text.startsWith('+')) {
          data.cell.styles.textColor = [52, 168, 83]; // #34A853
          data.cell.styles.fontStyle = 'bold';
        } else if (text.startsWith('-')) {
          data.cell.styles.textColor = [234, 67, 53]; // #EA4335
          data.cell.styles.fontStyle = 'bold';
        }
      }

      if (data.section === 'body' && data.column.index === 4) {
        const text = String(data.cell.raw || '');
        if (text === 'THU') {
          data.cell.styles.textColor = [52, 168, 83];
          data.cell.styles.fontStyle = 'bold';
        } else if (text === 'CHI') {
          data.cell.styles.textColor = [234, 67, 53];
          data.cell.styles.fontStyle = 'bold';
        }
      }
    },
  });

  const finalY = (doc as any).lastAutoTable?.finalY || yPos + 50;
  let summaryY = finalY + 10;

  if (summaryY + 40 > doc.internal.pageSize.getHeight() - 20) {
    doc.addPage();
    summaryY = 20;
  }

  doc.setFillColor(232, 240, 254); // #E8F0FE
  doc.roundedRect(margin, summaryY, pageWidth - margin * 2, 36, 3, 3, 'F');

  doc.setFillColor(26, 115, 232);
  doc.rect(margin, summaryY, 3, 36, 'F');

  summaryY += 8;
  doc.setFontSize(11);
  doc.setFont('Roboto', 'bold');
  doc.setTextColor(26, 115, 232);
  doc.text('TỔNG KẾT', margin + 8, summaryY);

  summaryY += 8;
  doc.setFontSize(9);
  doc.setFont('Roboto', 'normal');

  const colX1 = margin + 8;
  const colX2 = margin + 45;
  const colX3 = margin + 95;

  doc.setTextColor(95, 99, 104);
  doc.text('Tổng thu:', colX1, summaryY);
  doc.setTextColor(52, 168, 83);
  doc.setFont('Roboto', 'bold');
  doc.text(`+${formatCurrencyPlain(summary.totalIncome)} đ`, colX1, summaryY + 6);

  doc.setFont('Roboto', 'normal');
  doc.setTextColor(95, 99, 104);
  doc.text('Tổng chi:', colX2, summaryY);
  doc.setTextColor(234, 67, 53);
  doc.setFont('Roboto', 'bold');
  doc.text(`-${formatCurrencyPlain(summary.totalExpense)} đ`, colX2, summaryY + 6);

  doc.setFont('Roboto', 'normal');
  doc.setTextColor(95, 99, 104);
  doc.text('Số dư:', colX3, summaryY);
  doc.setTextColor(32, 33, 36);
  doc.setFont('Roboto', 'bold');
  doc.setFontSize(11);
  doc.text(`${formatCurrencyPlain(summary.balance)} đ`, colX3, summaryY + 6);

  const pageHeight = doc.internal.pageSize.getHeight();
  doc.setFontSize(8);
  doc.setFont('Roboto', 'normal');
  doc.setTextColor(150, 150, 150);
  doc.text(
    `Được tạo bởi Smart Finance Hub — ${today}`,
    pageWidth / 2,
    pageHeight - 10,
    { align: 'center' },
  );

  doc.setDrawColor(224, 224, 224);
  doc.setLineWidth(0.3);
  doc.line(margin, pageHeight - 15, pageWidth - margin, pageHeight - 15);

  doc.save(`${outputFileName}.pdf`);
}
