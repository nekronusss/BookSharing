import React, { useState } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import { qrcodeAPI } from '../services/api';
import { FiCopy, FiDownload } from 'react-icons/fi';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const QRCodeDisplay = ({ bookId, qrCode, title }) => {
  const [copied, setCopied] = useState(false);
  
  const qrCodeImageUrl = bookId ? `${API_BASE_URL}/qrcode/book/${bookId}` : null;

  const handleDownload = async () => {
    if (bookId) {
      try {
        const response = await qrcodeAPI.generateByBookId(bookId);
        const blob = response.data;
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `book-qr-${title || bookId}.png`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
      } catch (error) {
        console.error('Error downloading QR code:', error);
      }
    }
  };

  const handleCopyQRCode = () => {
    if (qrCode) {
      navigator.clipboard.writeText(qrCode);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  if (!qrCode) {
    return (
      <div className="p-4 bg-gray-50 rounded-lg">
        <p className="text-gray-500 text-sm">No QR code available for this book</p>
      </div>
    );
  }

  return (
    <div className="p-6 bg-white rounded-lg shadow-md border border-gray-200">
      <h3 className="text-lg font-semibold mb-4">Book QR Code</h3>
      
      <div className="flex flex-col items-center">
        {qrCodeImageUrl ? (
          <img
            src={qrCodeImageUrl}
            alt="Book QR Code"
            className="w-64 h-64 border border-gray-300 rounded-lg mb-4 object-contain"
          />
        ) : (
          <div className="w-64 h-64 flex items-center justify-center bg-white border border-gray-300 rounded-lg mb-4 p-4">
            <QRCodeSVG value={qrCode} size={256} />
          </div>
        )}

        <div className="mt-4 w-full max-w-xs">
          <div className="flex items-center justify-between bg-gray-50 p-3 rounded-lg mb-3">
            <span className="text-sm font-mono text-gray-700 truncate mr-2">{qrCode}</span>
            <button
              onClick={handleCopyQRCode}
              className="p-2 text-gray-600 hover:text-primary-600 transition-colors"
              title="Copy QR Code"
            >
              <FiCopy className="h-5 w-5" />
            </button>
          </div>
          {copied && (
            <p className="text-sm text-green-600 text-center mb-2">QR Code copied!</p>
          )}

          <div className="flex space-x-2">
            <button
              onClick={handleDownload}
              className="flex-1 flex items-center justify-center px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 transition-colors"
            >
              <FiDownload className="mr-2" />
              Download QR Code
            </button>
          </div>
        </div>

        <p className="text-xs text-gray-500 mt-4 text-center">
          Scan this QR code to view book details on another device
        </p>
      </div>
    </div>
  );
};

export default QRCodeDisplay;

