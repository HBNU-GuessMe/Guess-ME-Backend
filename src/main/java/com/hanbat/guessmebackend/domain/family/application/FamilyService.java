package com.hanbat.guessmebackend.domain.family.application;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hanbat.guessmebackend.domain.family.dto.FamilyInfoRequest;
import com.hanbat.guessmebackend.domain.family.dto.FamilyInfoResponse;
import com.hanbat.guessmebackend.domain.family.dto.FamilyUserInfoResponse;
import com.hanbat.guessmebackend.domain.family.entity.Family;
import com.hanbat.guessmebackend.domain.family.repository.FamilyRepository;
import com.hanbat.guessmebackend.domain.user.dto.UserCommonInfoResponse;
import com.hanbat.guessmebackend.domain.user.entity.User;
import com.hanbat.guessmebackend.domain.user.repository.UserRepository;
import com.hanbat.guessmebackend.global.error.exception.CustomException;
import com.hanbat.guessmebackend.global.error.exception.ErrorCode;
import com.hanbat.guessmebackend.global.jwt.MemberUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FamilyService {
	private final FamilyRepository familyRepository;
	private final UserRepository userRepository;
	private final MemberUtil memberUtil;

	@Transactional
	public FamilyInfoResponse connectFamily(FamilyInfoRequest familyInfoRequest) {
		final User ownerUser = memberUtil.getCurrentUser();

		Optional.of(familyInfoRequest.getCode())
			.filter((c) -> c.equals(ownerUser.getUserCode()))
			.ifPresentOrElse(
				c -> {},
				() -> { throw new CustomException(ErrorCode.FAMILY_CODE_IS_NOT_OWNER);});

		int count = familyInfoRequest.getUserIds().size();

		Family family = Family.builder()
			.familyCode(familyInfoRequest.getCode())
			.count(count)
			.build();

		familyRepository.save(family);

		familyInfoRequest.getUserIds().forEach(userId -> {
			User user = userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
			user.updateFamily(family);
			userRepository.save(user);
		});


		return FamilyInfoResponse.fromFamily(family, familyInfoRequest.getUserIds());

	}

	public FamilyUserInfoResponse getFamilyInfo(Long familyId) {

		Family family = familyRepository.findById(familyId)
			.orElseThrow(() -> new CustomException(ErrorCode.FAMILY_NOT_FOUND));

		List<User> users = familyRepository.findUsersByFamilyId(familyId)
			.orElseThrow(() -> new CustomException(ErrorCode.FAMILY_AND_USERS_NOT_FOUND));

		List<UserCommonInfoResponse> userResponses = users.stream()
			.map(UserCommonInfoResponse::fromUser)
			.toList();

		return FamilyUserInfoResponse.fromFamily(family, userResponses);
	}


}
