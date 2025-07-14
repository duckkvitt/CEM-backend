# 🖋️ Test Guide - Digital Signature System

## 🎯 **Mục tiêu**
Hướng dẫn test hệ thống chữ ký số đã được fix hoàn toàn để đảm bảo chữ ký hiển thị trong PDF.

## ⚡ **Quick Start Testing**

### 1. Khởi động Backend
```bash
cd CEM-backend/CEM-contract
mvn spring-boot:run
```

### 2. Tạo Test Certificate
```bash
curl -X POST http://localhost:8080/api/contracts/test/create-test-certificate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Test Digital Signature
```bash
curl -X POST http://localhost:8080/api/contracts/{CONTRACT_ID}/digital-signature \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "signerType": "MANAGER",
    "signerName": "Test Manager",
    "signerEmail": "manager@cem.com",
    "signatureData": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==",
    "pageNumber": 1,
    "signatureX": 400,
    "signatureY": 100,
    "signatureWidth": 200,
    "signatureHeight": 100,
    "reason": "Digital signature test",
    "location": "CEM Platform"
  }'
```

## 📊 **Test Cases Detail**

### Test Case 1: Basic Signature
**Mô tả**: Test cơ bản với chữ ký visible

**Endpoint**: `POST /api/contracts/{id}/digital-signature`

**Request Body**:
```json
{
  "signerType": "MANAGER",
  "signerName": "Test Signer",
  "signerEmail": "test@cem.com",
  "signatureData": "data:image/png;base64,iVBORw0KGg...", 
  "pageNumber": 1,
  "signatureX": 400,
  "signatureY": 100,
  "signatureWidth": 200,
  "signatureHeight": 100,
  "reason": "Digital signature",
  "location": "Digital Platform"
}
```

**Expected Result**: 
- ✅ Status 200 OK
- ✅ PDF có visible signature field tại vị trí (400, 100)
- ✅ Signature data được lưu trong database

### Test Case 2: Image Signature
**Mô tả**: Test với signature image từ canvas

**Frontend Code**:
```typescript
const canvas = document.getElementById('signatureCanvas') as HTMLCanvasElement;
const signatureData = canvas.toDataURL('image/png');

const request = {
  signerType: 'MANAGER',
  signerName: 'Manager User',
  signerEmail: 'manager@company.com',
  signatureData: signatureData,
  pageNumber: 1,
  signatureX: 50,
  signatureY: 650,
  signatureWidth: 200,
  signatureHeight: 100
};
```

### Test Case 3: Multiple Signatures
**Mô tả**: Test nhiều chữ ký trên cùng document

**Steps**:
1. Sign with MANAGER at position (50, 650)
2. Sign with CUSTOMER at position (300, 650) 
3. Verify both signatures appear in PDF

## 🔧 **Debugging Guide**

### Check Logs
```bash
tail -f CEM-backend/CEM-contract/logs/contract-out.log
```

### Database Verification
```sql
-- Check signatures created
SELECT * FROM digital_signature_records ORDER BY signed_at DESC;

-- Check contract status
SELECT id, contract_number, digital_signed, status FROM contracts WHERE id = YOUR_CONTRACT_ID;
```

### PDF Verification
1. Download signed PDF from endpoint
2. Open in Adobe Reader/Foxit Reader
3. Check signature panel (left sidebar)
4. Verify signature is visible on document

## 🚨 **Common Issues & Solutions**

### Issue: "No signature visible in PDF"
**Solution**: 
- Check coordinate system (Y starts from bottom)
- Verify image data is valid base64 PNG
- Check signature field position is within page bounds

### Issue: "Certificate not found"
**Solution**:
```bash
# Create test certificate first
curl -X POST http://localhost:8080/api/contracts/test/create-test-certificate
```

### Issue: "PDF not found"
**Solution**:
- Ensure contract has uploaded PDF file
- Check file path in contracts table
- Verify upload directory exists

## 📈 **Performance Expectations**

| Operation | Expected Time |
|-----------|---------------|
| Create Certificate | < 2 seconds |
| Sign PDF (small) | < 5 seconds |
| Sign PDF (large) | < 15 seconds |
| Verify Signature | < 3 seconds |

## ✅ **Success Criteria**

✅ PDF signature field visible in document  
✅ Signature data saved in database  
✅ Contract status updated to ACTIVE  
✅ No errors in application logs  
✅ Signature verifiable in PDF readers  

## 🔄 **Integration Testing**

### Frontend Integration
```typescript
// Test frontend signature modal
const handleSignContract = async (signatureImage: string) => {
  const response = await submitDigitalSignature(contractId, {
    signerType: 'MANAGER',
    signerName: currentUser.name,
    signerEmail: currentUser.email,
    signatureData: signatureImage,
    pageNumber: 1,
    signatureX: 400,
    signatureY: 100,
    signatureWidth: 200,
    signatureHeight: 100
  });
  
  if (response.success) {
    console.log('Signature added successfully');
    // Refresh contract data
  }
};
```

## 🎯 **Final Verification Steps**

1. **Visual Check**: Open signed PDF → See signature field
2. **Database Check**: Query signature records → Data exists  
3. **Log Check**: No errors in application logs
4. **Functional Check**: Contract status = ACTIVE
5. **Reader Check**: Adobe Reader shows valid signature

---

## 🎉 **Expected Results**

Khi test thành công, bạn sẽ thấy:

1. **Visible signature box** trong PDF tại vị trí đã chọn
2. **Signature information** hiển thị trong PDF reader  
3. **Contract status** chuyển thành "ACTIVE"
4. **Database records** cho signature đã tạo

**🔥 Chữ ký sẽ hiển thị rõ ràng trong PDF!** 